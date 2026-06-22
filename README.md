[![CI](https://github.com/assafko/MiniWSA/actions/workflows/ci.yml/badge.svg)](https://github.com/assafko/MiniWSA/actions/workflows/ci.yml)

# Mini Security Analytics (MiniWSA)

A Spring Boot backend service for ingesting, processing, enriching, and analyzing security events in real-time.

## Quick Start

### 1. Start PostgreSQL and Kafka (Docker Compose)
```bash
docker-compose up -d
# or use helper
./start-kafka.sh
```

### 2. Build & Run Application
```bash
./gradlew clean build
./gradlew bootRun
```

### 3. Test Event Ingestion
```bash
# Create a rule first
curl -X POST http://localhost:8080/api/v1/rules \
  -H "Content-Type: application/json" \
  -d '{
    "ruleId": "rule-sql-injection-001",
    "name": "SQL Injection Detection",
    "category": "INJECTION",
    "severity": "CRITICAL",
    "enabled": true
  }'

# Ingest an event
curl -X POST http://localhost:8080/api/v1/events/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "clientIp": "192.168.1.100",
    "path": "/api/users/login",
    "httpMethod": "POST",
    "action": "DENY",
    "ruleId": "rule-sql-injection-001",
    "timestamp": 1718901234567
  }'
```

## Features Implemented

### Kafka-backed async ingestion
- REST ingestion endpoints push events to Kafka (producer).
- A Kafka consumer reads from the topic and persists events via the service layer.
- Configured with 3 concurrent consumers (tunable) and JSON serialization.
- Dev Kafka broker via Docker Compose (KRaft mode).


### ✅ Part 1: PostgreSQL via Docker Compose
- PostgreSQL 15 Alpine container
- Named volume for persistence
- Pre-configured credentials in `.env`

### ✅ Part 2: Event Classification & Enrichment
- Attack type classification (7 categories)
- Threat score calculation (0-100) with:
  - Base severity scoring
  - Action bonuses (DENY, ALERT, MONITOR)
  - Path pattern detection (/admin, /login)
  - Repeat offender detection (>5 events in 10 min from same IP)
- Server-side `receivedAt` timestamp assignment
- Indexed database queries for performance

## Project Structure

```
MiniWSA/
├── docker-compose.yaml              # PostgreSQL service
├── pom.xml                          # Maven dependencies
├── src/main/java/com/miniwsa/
│   ├── controller/                  # REST endpoints
│   ├── domain/
│   │   ├── entity/                  # JPA entities
│   │   └── enums/                   # Domain enums
│   ├── dto/                         # Request/response DTOs
│   ├── exception/                   # Global exception handler
│   ├── repository/                  # Data access layer
│   └── service/
│       ├── EventService.java
│       └── classification/
│           ├── ClassificationService.java
│           ├── ThreatScoreCalculator.java
│           └── EventEnrichmentService.java
└── src/main/resources/
    └── application.yml              # Configuration
```

## REST API Endpoints

- `POST /api/v1/events/ingest` - Queue single event (202 Accepted)
- `POST /api/v1/events/ingest/batch` - Queue multiple events (202 Accepted)
- `GET /api/v1/events/samples` - List enriched events with filters (configId, from/to, category, action) and pagination (limit, offset)
- `GET /api/v1/stats/summary` - Aggregated statistics (query: from, to, optional configId)
- `POST /api/v1/rules` - Create rule
- `GET /api/v1/rules/{ruleId}` - Get rule
- `GET /api/v1/rules` - List rules
- `PUT /api/v1/rules/{ruleId}` - Update rule
- `DELETE /api/v1/rules/{ruleId}` - Delete rule

## Technology Stack

- **Java 17** with Spring Boot 3.2.0
- **PostgreSQL 15** (via Docker)
- **Apache Kafka 3.7** (Docker, KRaft) for async ingestion
- **Spring Kafka** (producer/consumer, JSON serialization)
- **Spring Data JPA** for persistence
- **Jakarta Bean Validation** for request validation
- **Gradle 8.5** for build management
- **Lombok** for reducing boilerplate

## Documentation

For detailed setup, configuration, and API usage, see [SETUP.md](./SETUP.md)

## Implemented Components

### Classification Service
Maps rule categories to human-readable attack types.

### Threat Score Calculator
Computes dynamic threat scores (0-100) considering:
- Rule severity (CRITICAL/HIGH/MEDIUM/LOW)
- Action taken (DENY/ALERT/MONITOR)
- Sensitive paths (/admin, /login)
- Repeat offender patterns

### Event Enrichment Service
Orchestrates classification and threat scoring, ensuring all enriched data is ready before persistence.

### Event Service
High-level orchestrator handling single/batch event ingestion with automatic enrichment and persistence.

## Data Generator

Generate realistic events with attack waves (NDJSON or post to API):

```bash
python3 scripts/generate_events.py --count 1000 --out events.ndjson
```

Post directly to API (batch 200):

```bash
python3 scripts/generate_events.py --count 5000 --post --batch-size 200 --api-base http://localhost:8080/api
```

Note: Ensure rules (e.g., rule-sql-injection-001) exist before posting.

## Next Steps

- [ ] Implement Rule Management API (CRUD)
- [ ] Add event retrieval and filtering
- [ ] Build analytics queries (trends, statistics)
- [ ] Add Kafka consumer for async ingestion
- [ ] Implement geolocation enrichment
- [ ] Add comprehensive test suite
- [ ] Implement API authentication

## License

Mini Security Analytics Pipeline
