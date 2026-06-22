# Mini Security Analytics (MiniWSA)

A Spring Boot backend service for ingesting, processing, enriching, and analyzing security events in real-time.

## Quick Start

### 1. Start PostgreSQL
```bash
docker-compose up -d
```

### 2. Build & Run Application
```bash
mvn clean package
mvn spring-boot:run
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

- `POST /api/v1/events/ingest` - Ingest single event (201 Created)
- `POST /api/v1/events/ingest/batch` - Ingest multiple events (201 Created)
- `GET /api/v1/stats/summary` - Aggregated statistics (query: from, to, optional configId)
- `POST /api/v1/rules` - Create rule (TBD)
- `GET /api/v1/rules/:ruleId` - Get rule (TBD)

## Technology Stack

- **Java 17** with Spring Boot 3.2.0
- **PostgreSQL 15** (via Docker)
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
