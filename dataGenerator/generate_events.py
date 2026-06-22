#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
MiniWSA Data Generator
Generates realistic-looking security events, including attack waves (bursts by IP/path).
Outputs NDJSON to stdout or a file, or posts directly to the ingestion API in batches.
"""
import argparse
import json
import random
import sys
import time
import uuid
from datetime import datetime, timedelta, timezone
from typing import List, Dict, Any
import urllib.request
import urllib.error

ACTIONS = ["DENY", "ALERT", "MONITOR"]
METHODS = ["GET", "POST", "PUT", "DELETE"]
PATHS = [
    "/api/v1/login",
    "/api/v1/users",
    "/api/v1/users/123",
    "/api/v1/admin",
    "/api/v1/health",
    "/api/v1/search",
    "/api/v1/orders",
]
USER_AGENTS = [
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
    "curl/7.79.1",
    "Go-http-client/1.1",
    "python-urllib/3.x",
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)"
]
RULE_IDS = [
    "rule-sql-injection-001",
    "rule-xss-001",
    "rule-bot-001",
    "rule-dos-001",
]

def rand_ip() -> str:
    return ".".join(str(random.randint(1, 254)) for _ in range(4))

def iso(ts: datetime) -> str:
    return ts.astimezone(timezone.utc).isoformat().replace("+00:00", "Z")

def choose_action(path: str) -> str:
    if any(s in path for s in ["/admin", "/login"]):
        return random.choices(ACTIONS, weights=[0.6, 0.3, 0.1])[0]
    return random.choice(ACTIONS)

def choose_rule_id(path: str) -> str:
    if "/login" in path:
        return "rule-sql-injection-001"
    if "/search" in path:
        return "rule-bot-001"
    return random.choice(RULE_IDS)

def gen_event(ts: datetime, ip: str = None, path: str = None, method: str = None, action: str = None,
              config_id: int = None) -> Dict[str, Any]:
    path = path or random.choice(PATHS)
    method = method or random.choice(METHODS)
    action = action or choose_action(path)
    ip = ip or rand_ip()
    rule_id = choose_rule_id(path)
    evt = {
        "eventId": str(uuid.uuid4()),
        "timestamp": iso(ts),
        "configId": config_id,
        "clientIp": ip,
        "path": path,
        "method": method,
        "action": action,
        "rule": {"id": rule_id},
        "userAgent": random.choice(USER_AGENTS),
        "payload": json.dumps({"sample": True, "path": path})
    }
    return {k: v for k, v in evt.items() if v is not None}

def make_waves(count: int, start: datetime, end: datetime, burst_prob: float,
               burst_min: int, burst_max: int, config_ids: List[int]) -> List[Dict[str, Any]]:
    events: List[Dict[str, Any]] = []
    window_secs = int((end - start).total_seconds())
    i = 0
    while i < count:
        if random.random() < burst_prob:
            k = min(count - i, random.randint(burst_min, burst_max))
            base_ts = start + timedelta(seconds=random.randint(0, max(1, window_secs)))
            ip = rand_ip()
            path = random.choice(PATHS)
            for _ in range(k):
                dt = timedelta(seconds=random.randint(0, 15))
                ts = base_ts + dt
                cfg = random.choice(config_ids) if config_ids else None
                events.append(gen_event(ts, ip=ip, path=path, action="DENY", config_id=cfg))
            i += k
        else:
            ts = start + timedelta(seconds=random.randint(0, max(1, window_secs)))
            cfg = random.choice(config_ids) if config_ids else None
            events.append(gen_event(ts, config_id=cfg))
            i += 1
    events.sort(key=lambda e: e["timestamp"]) 
    return events

def post_batches(api_base: str, batches: List[List[Dict[str, Any]]], throttle_ms: int, retries: int = 3) -> None:
    url = api_base.rstrip("/") + "/v1/events/ingest/batch"
    for idx, batch in enumerate(batches, start=1):
        data = json.dumps(batch).encode("utf-8")
        req = urllib.request.Request(url, data=data, headers={"Content-Type": "application/json"}, method="POST")
        attempt = 0
        while True:
            try:
                with urllib.request.urlopen(req, timeout=60) as resp:
                    _ = resp.read()
                break
            except urllib.error.HTTPError as e:
                attempt += 1
                if attempt >= retries:
                    print(f"Batch {idx}: HTTP {e.code} after {attempt} attempts", file=sys.stderr)
                    break
                time.sleep(min(5, attempt))
            except urllib.error.URLError as e:
                attempt += 1
                if attempt >= retries:
                    print(f"Batch {idx}: URL error {e.reason} after {attempt} attempts", file=sys.stderr)
                    break
                time.sleep(min(5, attempt))
        if throttle_ms > 0:
            time.sleep(throttle_ms / 1000.0)

def main():
    ap = argparse.ArgumentParser(description="Generate MiniWSA security events (NDJSON or POST)")
    ap.add_argument("--count", type=int, default=1000)
    ap.add_argument("--from", dest="from_iso", type=str, help="ISO8601 start time (default: now-24h)")
    ap.add_argument("--to", dest="to_iso", type=str, help="ISO8601 end time (default: now)")
    ap.add_argument("--seed", type=int, help="Random seed for reproducibility")
    ap.add_argument("--config-ids", type=int, nargs="*", default=[], help="List of config IDs to sample from")
    ap.add_argument("--burst-prob", type=float, default=0.2, help="Probability an event starts a burst")
    ap.add_argument("--burst-min", type=int, default=10, help="Min burst size")
    ap.add_argument("--burst-max", type=int, default=50, help="Max burst size")
    ap.add_argument("--out", type=str, help="NDJSON output file (default: stdout)")
    ap.add_argument("--post", action="store_true", help="POST directly to the ingestion API in batches")
    ap.add_argument("--batch-size", type=int, default=100)
    ap.add_argument("--throttle-ms", type=int, default=0, help="Sleep between batches in ms")
    ap.add_argument("--api-base", type=str, default="http://localhost:8080/api", help="API base (default: local)")
    args = ap.parse_args()

    if args.seed is not None:
        random.seed(args.seed)

    now = datetime.now(timezone.utc)
    start = datetime.fromisoformat(args.from_iso.replace("Z", "+00:00")) if args.from_iso else now - timedelta(hours=24)
    end = datetime.fromisoformat(args.to_iso.replace("Z", "+00:00")) if args.to_iso else now
    if start > end:
        print("Error: from must be <= to", file=sys.stderr)
        sys.exit(2)

    events = make_waves(args.count, start, end, args.burst_prob, args.burst_min, args.burst_max, args.config_ids)

    if args.post:
        batches = [events[i:i + args.batch_size] for i in range(0, len(events), args.batch_size)]
        post_batches(args.api_base, batches, args.throttle_ms)
        print(f"Posted {len(events)} events in {len(batches)} batches to {args.api_base}")
    else:
        out = open(args.out, "w") if args.out else sys.stdout
        try:
            for e in events:
                out.write(json.dumps(e) + "\n")
        finally:
            if out is not sys.stdout:
                out.close()

if __name__ == "__main__":
    main()
