#!/bin/bash

# Quick start guide for Kafka-enabled MiniWSA
# This script provides a quick way to get the system up and running

set -e

echo "╔═══════════════════════════════════════════════════════════════════╗"
echo "║           MiniWSA with Kafka Queue - Quick Start                 ║"
echo "╚═══════════════════════════════════════════════════════════════════╝"
echo ""

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Check if docker is installed
if ! command -v docker &> /dev/null; then
    echo -e "${YELLOW}⚠ Docker is not installed. Please install Docker first.${NC}"
    exit 1
fi

# Check if docker-compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo -e "${YELLOW}⚠ Docker Compose is not installed. Please install it first.${NC}"
    exit 1
fi

echo -e "${BLUE}Step 1: Starting Docker containers (PostgreSQL, Zookeeper, Kafka)${NC}"
docker-compose up -d
echo -e "${GREEN}✓ Docker containers started${NC}"
echo ""

# Wait for services to be ready
echo -e "${BLUE}Step 2: Waiting for services to be ready...${NC}"
sleep 10

# Check if Kafka is ready
MAX_ATTEMPTS=30
ATTEMPT=0
until docker exec miniwsa_kafka kafka-broker-api-versions.sh --bootstrap-server localhost:9092 &>/dev/null; do
    ATTEMPT=$((ATTEMPT+1))
    if [ $ATTEMPT -ge $MAX_ATTEMPTS ]; then
        echo -e "${YELLOW}⚠ Kafka failed to start${NC}"
        exit 1
    fi
    echo "  Waiting for Kafka... (attempt $ATTEMPT/$MAX_ATTEMPTS)"
    sleep 2
done
echo -e "${GREEN}✓ All services are ready${NC}"
echo ""

echo -e "${BLUE}Step 3: Building the application${NC}"
./gradlew clean build -x test > /dev/null 2>&1
echo -e "${GREEN}✓ Application built successfully${NC}"
echo ""

echo -e "${BLUE}Step 4: Starting the application${NC}"
echo -e "${YELLOW}Run this in a new terminal window:${NC}"
echo ""
echo -e "${GREEN}./gradlew bootRun${NC}"
echo ""
echo "The application will be available at: http://localhost:8080/api"
echo ""

echo -e "${BLUE}Step 5: Testing the implementation${NC}"
echo -e "${YELLOW}Once the application is running, test with:${NC}"
echo ""
echo -e "${GREEN}./test-kafka.sh${NC}"
echo ""

echo "╔═══════════════════════════════════════════════════════════════════╗"
echo "║                    Setup Complete!                               ║"
echo "╚═══════════════════════════════════════════════════════════════════╝"
echo ""

echo -e "${BLUE}📚 Documentation:${NC}"
echo "  • KAFKA-IMPLEMENTATION.md - Comprehensive Kafka documentation"
echo "  • KAFKA-SETUP-COMPLETE.md - Complete setup summary"
echo ""

echo -e "${BLUE}🔗 Useful Commands:${NC}"
echo "  View Docker services:        docker-compose ps"
echo "  View Kafka topics:           docker exec miniwsa_kafka kafka-topics.sh --bootstrap-server localhost:9092 --list"
echo "  View consumer lag:           docker exec miniwsa_kafka kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group security-events-group --describe"
echo "  Stop all services:           docker-compose down"
echo "  Stop and remove volumes:     docker-compose down -v"
echo ""

echo -e "${BLUE}📊 API Endpoints:${NC}"
echo "  Ingest single event:  POST http://localhost:8080/api/v1/events/ingest"
echo "  Ingest batch events:  POST http://localhost:8080/api/v1/events/ingest/batch"
echo ""

echo -e "${BLUE}💾 Database:${NC}"
echo "  Connection: psql postgresql://miniwsa_user:miniwsa_password@localhost/miniwsa_db"
echo "  View events: SELECT * FROM security_events ORDER BY created_at DESC LIMIT 20;"
echo ""

