# Quick Start Guide

## 🚀 Get Running in 5 Minutes

### Prerequisites
- Docker & Docker Compose installed
- Java 17+ installed
- Maven 3.6+ installed
- `curl` or Postman for API testing

### Step 1: Start PostgreSQL (30 seconds)
```bash
cd /Users/assafkozlowsky/IdeaProjects/MiniWSA

# Start the PostgreSQL container
docker-compose up -d

# Verify it's running
docker-compose ps
# Should show: miniwsa_postgres    Up (healthy)
```

### Step 2: Build & Run Application (2 minutes)
```bash
# Build the Spring Boot project
mvn clean package

# Run the application
mvn spring-boot:run
```

Wait until you see:
```
2026-06-21 10:30:45 Started MiniSecurityAnalyticsApplication in X seconds
```

### Step 3: Create a Sample Rule (30 seconds)
Open a new terminal and run:

```bash
curl -X POST http://localhost:8080/api/v1/rules \
  -H "Content-Type: application/json" \
  -d '{
    "ruleId": "rule-sql-injection-001",
    "name": "SQL Injection Detection",
    "description": "Detects SQL injection patterns",
    "category": "INJECTION",
    "severity": "CRITICAL",
    "enabled": true
  }'
```

✅ **Expected Response (201 Created)**:
```json
{
  "id": 1,
  "ruleId": "rule-sql-injection-001",
  "name": "SQL Injection Detection",
  "description": "Detects SQL injection patterns",
  "category": "INJECTION",
  "severity": "CRITICAL",
  "enabled": true,
  "createdAt": 1718901245890,
  "updatedAt": 1718901245890
}
```

### Step 4: Ingest Your First Security Event (30 seconds)

```bash
curl -X POST http://localhost:8080/api/v1/events/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "clientIp": "192.168.1.100",
    "path": "/api/users/login",
    "httpMethod": "POST",
    "action": "DENY",
    "payload": "{\"username\":\"admin\"}",
    "ruleId": "rule-sql-injection-001",
    "timestamp": 1718901234567
  }'
```

✅ **Expected Response (201 Created)**:
```json
{
  "id": 1,
  "clientIp": "192.168.1.100",
  "path": "/api/users/login",
  "httpMethod": "POST",
  "action": "DENY",
  "payload": "{\"username\":\"admin\"}",
  "ruleId": "rule-sql-injection-001",
  "ruleName": "SQL Injection Detection",
  "severity": "CRITICAL",
  "timestamp": 1718901234567,
  "receivedAt": 1718901245890,
  "attackType": "SQL/Command Injection",
  "threatScore": 75,
  "createdAt": 1718901245890
}
```

**Threat Score Breakdown**:
- Base (CRITICAL): 40
- Action (DENY): +20
- Path (/login): +15
- **Total: 75**

### Step 5: Test Threat Score Variations

**Test 1: High-Risk /admin Path**
```bash
curl -X POST http://localhost:8080/api/v1/events/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "clientIp": "10.0.0.50",
    "path": "/api/admin/settings",
    "httpMethod": "GET",
    "action": "DENY",
    "ruleId": "rule-sql-injection-001",
    "timestamp": 1718901334567
  }'
```
Expected threat score: **75** (40 + 20 + 15)

**Test 2: Lower Severity with ALERT**
```bash
curl -X POST http://localhost:8080/api/v1/events/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "clientIp": "172.16.0.1",
    "path": "/api/data",
    "httpMethod": "POST",
    "action": "ALERT",
    "ruleId": "rule-sql-injection-001",
    "timestamp": 1718901434567
  }'
```
Expected threat score: **50** (40 + 10 + 0)

**Test 3: MONITOR Action (No Action Bonus)**
```bash
curl -X POST http://localhost:8080/api/v1/events/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "clientIp": "203.0.113.45",
    "path": "/api/search",
    "httpMethod": "GET",
    "action": "MONITOR",
    "ruleId": "rule-sql-injection-001",
    "timestamp": 1718901534567
  }'
```
Expected threat score: **40** (40 + 0 + 0)

### Step 6: Batch Ingest Multiple Events

```bash
curl -X POST http://localhost:8080/api/v1/events/ingest/batch \
  -H "Content-Type: application/json" \
  -d '[
    {
      "clientIp": "192.168.1.100",
      "path": "/api/users",
      "httpMethod": "GET",
      "action": "MONITOR",
      "ruleId": "rule-sql-injection-001",
      "timestamp": 1718901634567
    },
    {
      "clientIp": "192.168.1.100",
      "path": "/api/admin/dashboard",
      "httpMethod": "GET",
      "action": "DENY",
      "ruleId": "rule-sql-injection-001",
      "timestamp": 1718901644567
    },
    {
      "clientIp": "192.168.1.100",
      "path": "/login",
      "httpMethod": "POST",
      "action": "ALERT",
      "ruleId": "rule-sql-injection-001",
      "timestamp": 1718901654567
    }
  ]'
```

✅ **Expected Response (201 Created)**: Array of 3 enriched events

### Step 7: Query Rules

**List All Rules**:
```bash
curl http://localhost:8080/api/v1/rules
```

**Get Specific Rule**:
```bash
curl http://localhost:8080/api/v1/rules/rule-sql-injection-001
```

## 🧪 Automated Testing

Run the complete test suite:
```bash
chmod +x test-events.sh
./test-events.sh
```

This will:
1. Create a SQL Injection rule
2. Test /admin path enrichment
3. Test /login path enrichment
4. Batch ingest events
5. Verify rule retrieval
6. List all rules

## 🐛 Debugging

### Check PostgreSQL Container
```bash
# View logs
docker-compose logs postgres

# Connect to database
docker exec -it miniwsa_postgres psql -U miniwsa_user -d miniwsa_db

# Query events in database
SELECT id, client_ip, path, attack_type, threat_score FROM security_events LIMIT 5;
```

### Check Application Logs
```bash
# Filter for errors
mvn spring-boot:run 2>&1 | grep -i "error\|exception"

# View full logs in another terminal
docker-compose logs -f  # PostgreSQL logs
mvn spring-boot:run     # Application logs
```

### Common Issues

**Port 5432 Already in Use**:
```bash
# Find what's using the port
lsof -i :5432

# Or use a different port in docker-compose.yaml
# Change: ports: - "5433:5432"
# Update: application.yml with jdbc:postgresql://localhost:5433/...
```

**Application Won't Start**:
```bash
# Ensure PostgreSQL is healthy
docker-compose ps
# Status should be "healthy"

# Check connectivity
telnet localhost 5432
```

**Gradle Build Fails**:
```bash
# Clean and rebuild
./gradlew clean build

# Check Java version
java -version  # Should be 17+

# Update Gradle dependencies
./gradlew dependencies
```

## 📊 Understanding Threat Scores

The threat score calculation combines multiple risk factors:

```
Base Severity Score (0-40):
  - CRITICAL = 40
  - HIGH = 30
  - MEDIUM = 20
  - LOW = 10

Action Bonus (0-20):
  - DENY = +20 (blocked attack)
  - ALERT = +10 (suspicious activity logged)
  - MONITOR = +0 (just observed)

Path Pattern (+15):
  - IF path contains "/admin" or "/login" = +15
  - Otherwise = 0

Repeat Offender (+15):
  - IF > 5 events from same IP in last 10 minutes = +15
  - Otherwise = 0

FINAL SCORE = MIN(sum of above, 100)
```

### Example Scenarios

| Scenario | Base | Action | Path | Repeat | Total |
|----------|------|--------|------|--------|-------|
| CRITICAL DENY on /login | 40 | 20 | 15 | 0 | 75 |
| CRITICAL DENY on /api/data | 40 | 20 | 0 | 0 | 60 |
| HIGH ALERT on /admin | 30 | 10 | 15 | 0 | 55 |
| MEDIUM MONITOR on normal path | 20 | 0 | 0 | 0 | 20 |
| CRITICAL DENY /login (5+ hits) | 40 | 20 | 15 | 15 | 90 |

## 📚 API Reference

### Rules Endpoints
| Method | Endpoint | Status | Purpose |
|--------|----------|--------|---------|
| POST | /v1/rules | 201 | Create rule |
| GET | /v1/rules | 200 | List all rules |
| GET | /v1/rules/{ruleId} | 200 | Get specific rule |
| PUT | /v1/rules/{ruleId} | 200 | Update rule |
| DELETE | /v1/rules/{ruleId} | 204 | Delete rule |

### Events Endpoints
| Method | Endpoint | Status | Purpose |
|--------|----------|--------|---------|
| POST | /v1/events/ingest | 201 | Ingest single event |
| POST | /v1/events/ingest/batch | 201 | Ingest multiple events |

## 🛑 Stopping

```bash
# Stop application (Ctrl+C in terminal)

# Stop PostgreSQL
docker-compose down

# Clean up database and restart fresh
docker-compose down -v
docker-compose up -d
```

## ✅ Success Indicators

You'll know everything is working when:
1. ✅ PostgreSQL container shows "healthy" status
2. ✅ Spring Boot starts with "Started MiniSecurityAnalyticsApplication"
3. ✅ Rule creation returns 201 with rule details
4. ✅ Event ingestion returns 201 with enriched data
5. ✅ Threat scores vary correctly based on inputs
6. ✅ Batch endpoint processes multiple events

## 📖 Full Documentation

- **[SETUP.md](./SETUP.md)** - Detailed setup and configuration
- **[IMPLEMENTATION.md](./IMPLEMENTATION.md)** - Architecture and design decisions
- **[README.md](./README.md)** - Project overview
- **[plan-eventClassificationEnrichment.prompt.md](./plan-eventClassificationEnrichment.prompt.md)** - Implementation plan

Enjoy! 🎉

