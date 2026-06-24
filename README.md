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

## Pipeline Architecture

```
Clients / Data Generator
        |
        v
     REST API
(/v1/events/ingest[/batch])
        |
        v
    Kafka topic
(security-events-topic)
        |
        v
 Kafka Consumers (x3)
(SecurityEventConsumer)
        |
        v
Enrichment & Scoring
(classification, threat)
        |
        v
    PostgreSQL
(security_events, rules)
        |
        v
 Read APIs / Analytics
- /v1/stats/summary
- /v1/events/samples
```

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

- `POST /api/v1/events/ingest` - Queue single event (201 Created)
- `POST /api/v1/events/ingest/batch` - Queue multiple events (201 Created)
- `GET /api/v1/events/samples` - List enriched events with filters (configId, from/to, category, action) and pagination (limit, offset)
- `GET /api/v1/stats/summary` - Aggregated statistics (query: from, to, optional configId)
- `POST /api/v1/rules` - Create rule
- `GET /api/v1/rules/{ruleId}` - Get rule
- `GET /api/v1/rules` - List rules
- `PUT /api/v1/rules/{ruleId}` - Update rule
- `DELETE /api/v1/rules/{ruleId}` - Delete rule
  
See postman-collections.json for postman collections

## Testing

### Unit + Web (default)
Runs fast tests (unit tests + `@WebMvcTest` controller tests):
```bash
./gradlew test
```

### Integration tests (SpringBootTest + Testcontainers)
We also have end-to-end API integration tests under `src/test/java/com/miniwsa/it/*IT.java`.
They run against **real Postgres + Kafka** via Testcontainers and are **opt-in** (requires Docker):
```bash
RUN_INTEGRATION_TESTS=true ./gradlew test
```

## Technology Stack

- **Java 17** with Spring Boot 3.2.0
- **PostgreSQL 15** (via Docker)
- **Apache Kafka 3.7** (Docker, KRaft) for async ingestion
- **Spring Kafka** (producer/consumer, JSON serialization)
- **Spring Data JPA** for persistence
- **Jakarta Bean Validation** for request validation
- **Gradle 8.5** for build management
- **Lombok** for reducing boilerplate
- **Testcontainers** (Postgres + Kafka) for integration tests

## Storage Architecture for Big Data Scale

### Why PostgreSQL + Kafka?

MiniWSA employs a **dual-storage strategy** optimized for high-volume security event ingestion and analytics:

#### 1. **Apache Kafka (Ingest & Buffering)**
- **Decoupled ingestion**: REST endpoints immediately publish to Kafka topics (non-blocking)
- **High throughput**: Handles burst traffic without blocking clients
- **Consumer parallelism**: 3+ concurrent consumers process events independently
- **Fault tolerance**: Kafka replicas (upgradeable to 3 for production) ensure no event loss
- **Scalability path**: Multi-broker cluster deployment for horizontal scaling

#### 2. **PostgreSQL (Long-term Analytics)**
- **Columnar indexing**: Composite index on `(client_ip, timestamp)` for fast time-range + IP queries
- **Partitioning strategy**: Table can be range-partitioned by `timestamp` for faster scans on large historical datasets
- **ACID compliance**: Ensures data integrity for audit and compliance requirements
- **Time-series optimized**: `timestamp` and `receivedAt` columns enable efficient filtering (days, weeks, months)
- **Enrichment storage**: All 25+ fields (threat_score, attack_type, geo_country) persist for detailed forensics

#### 3. **Why Not NoSQL for Everything?**
- **Compliance**: Security events require transaction guarantees and audit trails (PostgreSQL ACID model)
- **Analytics queries**: Complex aggregations across timestamps, IPs, and threat scores need SQL
- **Cost**: PostgreSQL scales efficiently for 100M+ events with proper indexing and partitioning

#### 4. **Big Data Growth Path**
- **Phase 1 (Current)**: Single PostgreSQL instance, Kafka with 3 partitions
- **Phase 2 (100M events)**: Enable table partitioning by `timestamp` (monthly/weekly)
- **Phase 3 (1B+ events)**: Federated databases (separate reads/writes) or time-series DB (TimescaleDB, Citus) as PostgreSQL extension
- **Phase 4 (Multi-region)**: Kafka multi-cluster replication + geographically distributed PostgreSQL replicas

#### 5. **Performance Optimizations Already In Place**
- **Indexes**: 3 indexes on security_events table (client_ip, timestamp, composite)
- **Batch ingestion**: `/events/ingest/batch` endpoint reduces I/O operations
- **Kafka partitioning**: 3 partitions enable parallel consumption
- **Lazy-loading**: ManyToOne relationship to Rule uses FetchType.LAZY
- **JSON serialization**: Kafka uses efficient JSON format, not Avro (upgradeable for stricter schemas)

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

Post directly to API (batch 200):
```bash
python3 scripts/generate_events.py --count 5000 --post --batch-size 200 --api-base http://localhost:8080/api --config-ids 1 2 3
```

Note: Ensure rules (e.g., rule-sql-injection-001) exist before posting.

## Next Steps

- [ ] Implement Rule Management API (CRUD)
- [ ] Add event retrieval and filtering
- [ ] Build analytics queries (trends, statistics)
- [ ] Index partitioning, sub-partitioning via Liquibase 
- [ ] Implement geolocation enrichment
- [ ] Add comprehensive test suite
- [ ] Implement API authentication

Mini Security Analytics Pipeline
