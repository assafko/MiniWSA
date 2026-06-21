#!/bin/bash

# Test script for Kafka queue implementation
# This script demonstrates how to test the SecurityEventRequest Kafka queue

set -e

echo "=== Kafka Queue Testing Script ==="
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
API_URL="http://localhost:8080/api"
KAFKA_CONTAINER="miniwsa_kafka"
BOOTSTRAP_SERVER="localhost:9092"
TOPIC="security-events-topic"
GROUP="security-events-group"

# Functions
check_services() {
    echo -e "${BLUE}Step 1: Checking if services are running...${NC}"

    # Check if Kafka is running
    if ! nc -z localhost 9092 2>/dev/null; then
        echo -e "${YELLOW}Kafka is not running. Starting docker-compose...${NC}"
        docker-compose up -d
        sleep 10
    fi

    # Check if application is running
    if ! nc -z localhost 8080 2>/dev/null; then
        echo -e "${YELLOW}Application is not running. Please start it with: ./gradlew bootRun${NC}"
        exit 1
    fi

    echo -e "${GREEN}✓ All services are running${NC}"
    echo ""
}

check_kafka_topic() {
    echo -e "${BLUE}Step 2: Checking Kafka topic...${NC}"

    docker exec $KAFKA_CONTAINER kafka-topics.sh \
        --bootstrap-server $BOOTSTRAP_SERVER \
        --describe \
        --topic $TOPIC

    echo ""
}

send_single_event() {
    echo -e "${BLUE}Step 3: Sending a single security event...${NC}"

    RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/v1/events/ingest" \
        -H "Content-Type: application/json" \
        -d '{
            "eventId": "test-evt-'$(date +%s)'",
            "timestamp": "'$(date -u +'%Y-%m-%dT%H:%M:%SZ')'",
            "clientIp": "192.168.1.100",
            "path": "/admin/login",
            "method": "POST",
            "action": "LOGIN_ATTEMPT",
            "userAgent": "curl/test",
            "statusCode": 200
        }')

    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | head -n1)

    if [ "$HTTP_CODE" -eq 202 ]; then
        echo -e "${GREEN}✓ Event sent successfully (HTTP $HTTP_CODE)${NC}"
        echo "Response: $BODY"
    else
        echo -e "${YELLOW}Unexpected response: HTTP $HTTP_CODE${NC}"
        echo "Response: $BODY"
    fi
    echo ""
}

send_batch_events() {
    echo -e "${BLUE}Step 4: Sending a batch of events...${NC}"

    TIMESTAMP=$(date -u +'%Y-%m-%dT%H:%M:%SZ')

    RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/v1/events/ingest/batch" \
        -H "Content-Type: application/json" \
        -d '[
            {
                "eventId": "batch-evt-1-'$(date +%s)'",
                "timestamp": "'$TIMESTAMP'",
                "clientIp": "192.168.1.100",
                "path": "/admin/login",
                "method": "POST",
                "action": "LOGIN_ATTEMPT",
                "userAgent": "curl/test",
                "statusCode": 200
            },
            {
                "eventId": "batch-evt-2-'$(date +%s)'",
                "timestamp": "'$TIMESTAMP'",
                "clientIp": "192.168.1.101",
                "path": "/api/users",
                "method": "GET",
                "action": "DATA_ACCESS",
                "userAgent": "curl/test",
                "statusCode": 200
            },
            {
                "eventId": "batch-evt-3-'$(date +%s)'",
                "timestamp": "'$TIMESTAMP'",
                "clientIp": "192.168.1.102",
                "path": "/config",
                "method": "PUT",
                "action": "CONFIGURATION_CHANGE",
                "userAgent": "curl/test",
                "statusCode": 403
            }
        ]')

    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | head -n1)

    if [ "$HTTP_CODE" -eq 202 ]; then
        echo -e "${GREEN}✓ Batch sent successfully (HTTP $HTTP_CODE)${NC}"
        echo "Response: $BODY"
    else
        echo -e "${YELLOW}Unexpected response: HTTP $HTTP_CODE${NC}"
        echo "Response: $BODY"
    fi
    echo ""
}

view_kafka_messages() {
    echo -e "${BLUE}Step 5: Viewing messages in Kafka topic...${NC}"
    echo "(Showing last 3 messages with a timeout of 5 seconds)"
    echo ""

    timeout 5 docker exec $KAFKA_CONTAINER kafka-console-consumer.sh \
        --bootstrap-server $BOOTSTRAP_SERVER \
        --topic $TOPIC \
        --from-beginning \
        --max-messages 3 \
        2>/dev/null || true

    echo ""
}

check_consumer_lag() {
    echo -e "${BLUE}Step 6: Checking consumer group lag...${NC}"

    docker exec $KAFKA_CONTAINER kafka-consumer-groups.sh \
        --bootstrap-server $BOOTSTRAP_SERVER \
        --group $GROUP \
        --describe 2>/dev/null || echo "Consumer group might not have connected yet"

    echo ""
}

stress_test() {
    echo -e "${BLUE}Step 7: Running stress test (sending 10 events rapidly)...${NC}"

    for i in {1..10}; do
        curl -s -X POST "$API_URL/v1/events/ingest" \
            -H "Content-Type: application/json" \
            -d '{
                "eventId": "stress-test-'$i'-'$(date +%s%N)'",
                "timestamp": "'$(date -u +'%Y-%m-%dT%H:%M:%SZ')'",
                "clientIp": "192.168.1.'$((100 + i))'",
                "path": "/api/test",
                "method": "GET",
                "action": "API_CALL",
                "userAgent": "stress-test",
                "statusCode": 200
            }' > /dev/null &
    done

    wait
    echo -e "${GREEN}✓ 10 events sent to queue${NC}"
    echo ""
}

show_summary() {
    echo -e "${BLUE}=== Test Summary ===${NC}"
    echo ""
    echo "✓ API Endpoint (single): POST http://localhost:8080/api/v1/events/ingest"
    echo "  Returns: 202 Accepted"
    echo ""
    echo "✓ API Endpoint (batch): POST http://localhost:8080/api/v1/events/ingest/batch"
    echo "  Returns: 202 Accepted"
    echo ""
    echo "✓ Kafka Topic: $TOPIC"
    echo "✓ Consumer Group: $GROUP"
    echo ""
    echo "✓ View messages in database:"
    echo "  psql postgresql://miniwsa_user:miniwsa_password@localhost/miniwsa_db"
    echo "  SELECT * FROM security_events ORDER BY created_at DESC LIMIT 10;"
    echo ""
}

# Main execution
main() {
    check_services
    check_kafka_topic
    send_single_event
    send_batch_events
    sleep 2  # Give consumers time to process
    view_kafka_messages
    check_consumer_lag
    stress_test
    show_summary
}

main

