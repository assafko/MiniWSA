# Mini Security Analytics Pipeline Backend

A Spring Boot backend service for ingesting, processing, enriching, and analyzing security events.

## Features

### Part 1: Docker Compose PostgreSQL Setup
- PostgreSQL 15 Alpine container with persistent volume storage
- Pre-configured database, user, and credentials
- Named volume for data persistence across container restarts
- Health checks for reliable startup

### Part 2: Event Classification & Enrichment
- **Attack Type Classification**: Maps rule categories to human-readable attack types
  - INJECTION → SQL/Command Injection
  - XSS → Cross-Site Scripting
  - PROTOCOL_VIOLATION → Protocol Anomaly
  - DATA_LEAKAGE → Data Exfiltration
  - BOT → Bot Activity
  - DOS → Denial of Service
  - RATE_LIMIT → Rate Limiting

- **Threat Score Calculation** (0-100):
  - Base score from severity: CRITICAL=40, HIGH=30, MEDIUM=20, LOW=10
  - Action bonus: DENY=+20, ALERT=+10, MONITOR=+0
  - Path pattern detection: +15 for paths containing /admin or /login
  - Repeat offender bonus: +15 if >5 events from same IP within 10 minutes
  - Final score capped at 100

- **Event Enrichment**: Transforms raw events with original fields + `attackType`, `threatScore`, and `receivedAt` timestamp

## Project Structure

```
├── docker-compose.yaml          # PostgreSQL service definition
├── pom.xml                       # Maven dependencies
├── src/main/java/com/miniwsa/
│   ├── MiniSecurityAnalyticsApplication.java
│   ├── controller/
│   │   └── EventController.java         # REST endpoints for event ingestion
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
│   │   └── SecurityEventResponse.java
│   ├── exception/
│   │   └── GlobalExceptionHandler.java
│   ├── repository/
│   │   ├── RuleRepository.java
│   │   ├── SecurityEventRepository.java
│   │   └── GeoLocationRepository.java
│   └── service/
│       ├── EventService.java
│       └── classification/
│           ├── ClassificationService.java
│           ├── ThreatScoreCalculator.java
│           └── EventEnrichmentService.java
└── src/main/resources/
    └── application.yml           # Spring Boot configuration
```

## Setup & Running

### Prerequisites
- Docker & Docker Compose
- Java 17+
- Maven 3.6+

### Step 1: Start PostgreSQL
```bash
docker-compose up -d
```

Verify container is running:
```bash
docker-compose ps
```

Reset database (if needed):
```bash
docker-compose down -v
docker-compose up -d
```

### Step 2: Build & Run Spring Boot Application
```bash
# Build the project
mvn clean package

# Run the application
```

The application will start on `http://localhost:8080/api`

### Step 3: Create a Rule (Required for Event Ingestion)
Before ingesting events, create a rule:

```bash
curl -X POST http://localhost:8080/api/v1/rules \
  -H "Content-Type: application/json" \
  -d '{
    "ruleId": "rule-sql-injection-001",
    "name": "SQL Injection Detection",
    "description": "Detects common SQL injection patterns",
    "category": "INJECTION",
    "severity": "CRITICAL",
    "enabled": true
  }'
```

### Step 4: Ingest a Security Event
```bash
curl -X POST http://localhost:8080/api/v1/events/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "clientIp": "192.168.1.100",
    "path": "/api/users/login",
    "httpMethod": "POST",
    "action": "DENY",
    "payload": "{\"username\":\"admin\",\"password\":\"test\"}",
    "ruleId": "rule-sql-injection-001",
    "timestamp": 1718901234567
  }'
```

**Response (201 Created):**
```json
{
  "id": 1,
  "clientIp": "192.168.1.100",
  "path": "/api/users/login",
  "httpMethod": "POST",
  "action": "DENY",
  "payload": "{\"username\":\"admin\",\"password\":\"test\"}",
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

### Step 5: Batch Ingest Events
```bash
curl -X POST http://localhost:8080/api/v1/events/ingest/batch \
  -H "Content-Type: application/json" \
  -d '[
    {
      "clientIp": "192.168.1.100",
      "path": "/admin/dashboard",
      "httpMethod": "GET",
      "action": "ALERT",
      "ruleId": "rule-sql-injection-001",
      "timestamp": 1718901234567
    },
    {
      "clientIp": "10.0.0.50",
      "path": "/api/search",
      "httpMethod": "POST",
      "action": "MONITOR",
      "payload": "<script>alert('xss')</script>",
      "ruleId": "rule-sql-injection-001",
      "timestamp": 1718901244567
    }
  ]'
```

## Threat Score Calculation Example

**Scenario**: Event with CRITICAL severity, DENY action, /login path, and 6 recent events from same IP

```
Base score (CRITICAL):        40
Action bonus (DENY):          +20
Path bonus (/login):          +15
Repeat offender bonus:        +15
─────────────────────────────
Calculated score:             90 (capped at 100)
```

## Database Schema

### Rules Table
- `id`: Primary key
- `rule_id`: Unique rule identifier (e.g., "rule-sql-injection-001")
- `name`: Human-readable rule name
- `description`: Rule details
- `category`: RuleCategory enum (INJECTION, XSS, etc.)
- `severity`: Severity enum (CRITICAL, HIGH, MEDIUM, LOW)
- `enabled`: Boolean flag
- `created_at`, `updated_at`: Timestamps

### Security Events Table
- `id`: Primary key
- `client_ip`: Source IP address (indexed)
- `path`: Request path
- `http_method`: HTTP method (GET, POST, etc.)
- `action`: Action taken (DENY, ALERT, MONITOR)
- `payload`: Request payload (optional)
- `rule_id`: Foreign key to rules table
- `timestamp`: Event timestamp (client-provided)
- `received_at`: Server-side ingestion timestamp (indexed)
- `attack_type`: Enriched attack type classification
- `threat_score`: Calculated threat score (0-100)
- `created_at`: Record creation timestamp
- **Indexes**: `client_ip`, `received_at`, composite `(client_ip, received_at)` for efficient repeat offender queries

### Geo Locations Table
- `id`: Primary key
- `ip_address`: IP address (unique)
- `country`: Country name
- `city`: City name
- `region`: Region/state
- `latitude`, `longitude`: Geographic coordinates
- `created_at`: Timestamp

## Error Handling

### Validation Errors (400 Bad Request)
Missing required fields or invalid values:
```json
{
  "timestamp": "2026-06-21T10:30:45",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "clientIp": "clientIp is required",
    "action": "action is required"
  }
}
```

### Rule Not Found (400 Bad Request)
```json
{
  "timestamp": "2026-06-21T10:30:45",
  "status": 400,
  "error": "Bad Request",
  "message": "Rule not found: invalid-rule-id"
}
```

### Server Errors (500 Internal Server Error)
Unexpected errors during processing.

## Configuration

Edit `src/main/resources/application.yml` to customize:
- Database connection details (url, username, password)
- JPA/Hibernate settings
- Logging levels
- Server port (default: 8080)

## Further Development

- [ ] Implement Rule Management API (CRUD endpoints)
- [ ] Add event retrieval/filtering APIs
- [ ] Implement analytics queries (trends, statistics)
- [ ] Add Kafka consumer for async event ingestion
- [ ] Implement geolocation enrichment logic
- [ ] Add unit and integration tests
- [ ] Implement API authentication/authorization
- [ ] Add request/response logging middleware

## Troubleshooting

### PostgreSQL connection fails
```bash
# Check if container is running
docker-compose ps

# Check logs
docker-compose logs postgres

# Ensure port 5432 is not in use
lsof -i :5432
```

### Application fails to start
```bash
# Check Maven build
mvn clean compile

# Verify Java version
java -version  # Should be 17+

# Check application logs
mvn spring-boot:run | tail -50
```

**Gradle alternative for testing**:
To use Gradle instead of Maven throughout the project, all build commands use `./gradlew`:
```bash
# Build
./gradlew clean build

# Run
./gradlew bootRun

# Run tests
./gradlew test

# Build JAR
./gradlew bootJar
```

## License

This project is part of the Mini Security Analytics Pipeline.

