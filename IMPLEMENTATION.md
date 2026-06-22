# Implementation Summary: Event Classification & Enrichment Service

## Overview

Successfully implemented a complete Spring Boot-based Event Classification & Enrichment Service for the Mini Security Analytics (MiniWSA) pipeline. The implementation includes:

1. **Docker Compose PostgreSQL Setup** - Containerized database with persistence
2. **Event Classification** - Maps rule categories to human-readable attack types
3. **Threat Score Calculation** - Dynamic scoring (0-100) based on multiple factors
4. **Event Enrichment Pipeline** - Orchestrates classification, scoring, and persistence
5. **REST API Endpoints** - Event ingestion (single & batch) and rule management

## Component Architecture

### Domain Layer (`domain/`)

#### Enums
- **`RuleCategory`**: INJECTION, XSS, PROTOCOL_VIOLATION, DATA_LEAKAGE, BOT, DOS, RATE_LIMIT
- **`Severity`**: CRITICAL (40), HIGH (30), MEDIUM (20), LOW (10) - with base scores
- **`Action`**: DENY, ALERT, MONITOR

#### Entities
- **`Rule`**: Security rules with category, severity, and enable/disable flag
- **`SecurityEvent`**: Core entity with enriched fields (attackType, threatScore, receivedAt)
- **`GeoLocation`**: Geolocation mapping for IP addresses (for future enrichment)

### Service Layer (`service/`)

#### Classification Services

**`ClassificationService`**
```java
public String classifyAttackType(RuleCategory category)
```
- Maps RuleCategory enums to human-readable attack type strings
- Returns "Unknown" for null categories

**`ThreatScoreCalculator`**
- Computes threat scores (0-100) using composite scoring algorithm:
  - Base severity score (40-10 points)
  - Action bonus (DENY=+20, ALERT=+10, MONITOR=+0)
  - Path pattern detection (+15 for /admin or /login paths)
  - Repeat offender bonus (+15 if >5 events from same IP in last 10 minutes)
  - Final score capped at 100

- **Repeat Offender Query**: Indexed query on `(clientIp, receivedAt DESC)` for O(log n) lookup

**`EventEnrichmentService`**
- Orchestrates end-to-end event enrichment
- Resolves rule by ruleId
- Calls ClassificationService for attack type
- Calls ThreatScoreCalculator for threat score
- Sets server-side `receivedAt` timestamp
- Returns fully enriched SecurityEvent entity ready for persistence
- Transaction scope: `@Transactional(readOnly = true)` for consistency

#### High-Level Services

**`EventService`**
- `ingestEvent(SecurityEventRequest)` → SecurityEventResponse
- `ingestBatchEvents(List<SecurityEventRequest>)` → List<SecurityEventResponse>
- Orchestrates enrichment + persistence
- Transaction scope: `@Transactional` for atomicity

**`RuleService`**
- `createRule(RuleRequest)` - Create new rule with uniqueness check
- `getRuleByRuleId(String)` - Retrieve rule
- `getAllRules()` - List all rules
- `updateRule(String, RuleRequest)` - Update existing rule
- `deleteRule(String)` - Delete rule

### Repository Layer (`repository/`)

- **`RuleRepository`**: JPA repository with `findByRuleId(String)` custom query
- **`SecurityEventRepository`**: 
  - Custom query: `findRecentEventsByClientIp(clientIp, tenMinutesAgo)` for repeat offender detection
  - Optimized with composite index: `(client_ip, timestamp)`
- **`GeoLocationRepository`**: `findByIpAddress(String)` for geolocation lookups

### Data Transfer Layer (`dto/`)

**Request DTOs**
- `SecurityEventRequest`: clientIp, path, httpMethod, action, payload, ruleId, timestamp (validated)
- `RuleRequest`: ruleId, name, description, category, severity, enabled (validated)

**Response DTOs**
- `SecurityEventResponse`: Enriched event with attackType, threatScore, receivedAt
- `RuleResponse`: Rule details with timestamps

### Controller Layer (`controller/`)

**`EventController`**
- `POST /v1/events/ingest` → 201 Created
- `POST /v1/events/ingest/batch` → 201 Created

**`RuleController`**
- `POST /v1/rules` → 201 Created
- `GET /v1/rules/{ruleId}` → 200 OK
- `GET /v1/rules` → 200 OK (list all)
- `PUT /v1/rules/{ruleId}` → 200 OK
- `DELETE /v1/rules/{ruleId}` → 204 No Content

### Exception Handling (`exception/`)

**`GlobalExceptionHandler`**
- Validation errors (400): Field-level error messages
- Illegal argument (400): Rule not found, etc.
- Generic exceptions (500): Internal server errors

## Database Schema

### Rules Table (`rules`)
```sql
id (PK) | rule_id (UNIQUE) | name | description | category | severity | enabled | created_at | updated_at
```

### Security Events Table (`security_events`)
```sql
id (PK) | client_ip | path | http_method | action | payload | rule_id (FK) | timestamp | received_at | attack_type | threat_score | created_at
```

**Indexes**:
- `idx_client_ip` on `client_ip`
- `idx_timestamp` on `timestamp`
- `idx_client_ip_timestamp` composite on `(client_ip, timestamp)`

### Geo Locations Table (`geo_locations`)
```sql
id (PK) | ip_address (UNIQUE) | country | city | region | latitude | longitude | created_at
```

## Configuration

### `docker-compose.yaml`
- PostgreSQL 15 Alpine image
- Database: `miniwsa_db`
- User/Password: `miniwsa_user` / `miniwsa_password`
- Persistent volume: `postgres_data`
- Health check: `pg_isready` every 10 seconds

### `application.yml`
```yaml
spring.jpa.hibernate.ddl-auto: update  # Auto schema creation/update
spring.datasource.url: jdbc:postgresql://localhost:5432/miniwsa_db
spring.sql.init.mode: always           # Load data.sql on startup
server.servlet.context-path: /api
```

### `.env`
- Database credentials for docker-compose

### `data.sql`
- Pre-loaded 7 sample rules for testing:
  - SQL Injection (CRITICAL)
  - XSS (HIGH)
  - DoS (HIGH)
  - Bot Activity (MEDIUM)
  - Data Leakage (CRITICAL)
  - Rate Limiting (MEDIUM)
  - Protocol Anomaly (LOW)

## Threat Score Example

**Scenario**: CRITICAL rule, DENY action, /login path, 6+ recent events from same IP

```
Base severity (CRITICAL):     40
Action bonus (DENY):          +20
Path bonus (/login):          +15
Repeat offender bonus:        +15
─────────────────────────────
Raw score:                    90 (≤ 100, no capping needed)
Final threat score:           90
```

## API Usage Examples

### 1. Create Rule
```bash
curl -X POST http://localhost:8080/api/v1/rules \
  -H "Content-Type: application/json" \
  -d '{
    "ruleId": "rule-xss-001",
    "name": "XSS Detection",
    "category": "XSS",
    "severity": "HIGH",
    "enabled": true
  }'
```

### 2. Ingest Single Event
```bash
curl -X POST http://localhost:8080/api/v1/events/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "clientIp": "192.168.1.100",
    "path": "/api/users/login",
    "httpMethod": "POST",
    "action": "DENY",
    "ruleId": "rule-xss-001",
    "timestamp": 1718901234567
  }'
```

### 3. Batch Ingest
```bash
curl -X POST http://localhost:8080/api/v1/events/ingest/batch \
  -H "Content-Type: application/json" \
  -d '[{...}, {...}]'
```

## Performance Optimizations

1. **Indexed Queries**: Composite index on (client_ip, timestamp) for O(log n) repeat offender lookups
2. **Read-Only Transactions**: Classification and enrichment services use `readOnly=true` for potential database optimizations
3. **In-Bulk Loading**: Batch endpoint processes multiple events in single transaction
4. **Lazy Fetching**: Rule entity uses lazy loading to prevent N+1 queries

## Validation & Error Handling

- **Request Validation**: Jakarta Bean Validation on all request DTOs
- **Business Logic Validation**: Rule uniqueness checks, rule existence validation
- **Error Responses**: Structured JSON error responses with timestamps and field-level messages

## Files Created

```
MiniWSA/
├── build.gradle                 # Gradle build configuration
├── settings.gradle              # Gradle settings
├── gradle/wrapper/
│   └── gradle-wrapper.properties # Gradle wrapper configuration
├── gradlew                       # Gradle wrapper (Unix/Mac)
├── docker-compose.yaml
├── .env
├── SETUP.md
├── test-events.sh
├── src/main/java/com/miniwsa/
│   ├── MiniSecurityAnalyticsApplication.java
│   ├── controller/
│   │   ├── EventController.java
│   │   └── RuleController.java
│   ├── domain/
│   │   ├── entity/
│   │   │   ├── Rule.java
│   │   │   ├── SecurityEvent.java
│   │   │   └── GeoLocation.java
│   │   └── enums/
│   │       ├── RuleCategory.java
│   │       ├── Severity.java
│   │       └── Action.java
│   ├── dto/
│   │   ├── SecurityEventRequest.java
│   │   ├── SecurityEventResponse.java
│   │   ├── RuleRequest.java
│   │   └── RuleResponse.java
│   ├── exception/
│   │   └── GlobalExceptionHandler.java
│   ├── repository/
│   │   ├── RuleRepository.java
│   │   ├── SecurityEventRepository.java
│   │   └── GeoLocationRepository.java
│   └── service/
│       ├── EventService.java
│       ├── RuleService.java
│       └── classification/
│           ├── ClassificationService.java
│           ├── ThreatScoreCalculator.java
│           └── EventEnrichmentService.java
└── src/main/resources/
    ├── application.yml
    └── data.sql
```

## Key Design Decisions

1. **Enrichment Scope**: Classification and threat scoring happen synchronously during event ingestion for immediate consistency
2. **Repeat Offender Lookup**: Database query with time-window filtering (10 minutes) for accuracy and scalability
3. **Path Detection**: Case-insensitive substring matching on /admin and /login
4. **Score Composition**: Additive model with hard cap at 100 for simplicity and predictability
5. **Transaction Management**: Atomic transactions ensure enrichment and persistence are consistent
6. **Batch Processing**: Batch endpoint processes all events in single transaction for efficiency

## Future Enhancements

- [ ] Async event processing via Kafka consumer
- [ ] Geolocation enrichment lookup (MaxMind GeoIP2)
- [ ] Event retrieval and filtering APIs
- [ ] Analytics queries (trends, statistics by country/category/severity)
- [ ] Risk scoring refinement based on ML models
- [ ] Event deduplication logic
- [ ] API authentication (OAuth 2.0, API keys)
- [ ] Rate limiting middleware
- [ ] Comprehensive test suite (unit, integration, load tests)

## Running the Implementation

```bash
# 1. Start PostgreSQL
docker-compose up -d

# 2. Build
./gradlew clean build

# 3. Run
./gradlew bootRun

# 4. Test (from another terminal)
chmod +x test-events.sh
./test-events.sh
```

Application runs on: `http://localhost:8080/api`

