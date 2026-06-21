## Plan: Event Classification & Enrichment Service

**TL;DR:** Implement a classification and threat scoring engine that processes ingested security events by mapping rule categories to attack types and computing dynamic threat scores (0-100) based on severity, action, path patterns, and repeat offender detection. Create an enrichment service layer that transforms raw events into enriched records with `attackType`, `threatScore`, and `receivedAt` fields before persistence.

### Steps

1. **Extend domain entities** with new fields: add `attackType` (String), `threatScore` (Integer 0-100), and ensure `receivedAt` (Timestamp) exists in SecurityEvent entity; create corresponding DTO updates.

2. **Create enums for classification mapping**: `RuleCategory` enum (INJECTION, XSS, PROTOCOL_VIOLATION, DATA_LEAKAGE, BOT, DOS, RATE_LIMIT) and `Severity` enum (CRITICAL, HIGH, MEDIUM, LOW) with their scoring values as constants.

3. **Build `ClassificationService`** with static mapping method `classifyAttackType(RuleCategory)` returning human-readable attack type strings per the provided mapping table.

4. **Implement `ThreatScoreCalculator`** component that computes scores by: (a) base severity points, (b) action bonus (+20 DENY, +10 ALERT, +0 MONITOR), (c) path pattern detection (+15 for /admin or /login), (d) repeat offender check via repository query (clientIp events in last 10 minutes), capping final result at 100.

5. **Create `EventEnrichmentService`** that orchestrates classification and threat scoring, calling repository for repeat offender data, and returns enriched event object ready for persistence.

6. **Integrate enrichment into ingestion flow**: modify event ingestion endpoint/handler to call `EventEnrichmentService` before saving to database, ensuring all three fields (`attackType`, `threatScore`, `receivedAt`) are populated.

7. **Add database indexes** on `clientIp` and `timestamp` (or `receivedAt`) columns to optimize repeat offender queries within the 10-minute window.

### Further Considerations

1. **Repeat offender query performance:** Should repeat-offender lookup query against all events or a rolling time-series table? → Recommend indexed query on `clientIp` + `receivedAt DESC LIMIT` for efficiency; consider caching if query volume is high.

2. **Threat score composition:** Are bonus criteria (path, repeat offender) required together or independently? Should they stack additively as specified? → Confirm stacking behavior; plan for future extensibility (e.g., geolocation-based bonuses).

3. **Event enrichment transactionality:** Should enrichment happen in same transaction as event save, or could enrichment fail without blocking ingestion? → Recommend same transaction for data consistency; enrich before save attempt.

