#!/bin/bash

# Mini Security Analytics - Event Ingestion Test Script
# Tests the classification and enrichment implementation

API_BASE="http://localhost:8080/api"

echo "=========================================="
echo "MiniWSA - Event Classification & Enrichment Test"
echo "=========================================="
echo ""

# Test 1: Create a rule
echo "1️⃣  Creating SQL Injection Rule..."
RULE_RESPONSE=$(curl -s -X POST "$API_BASE/v1/rules" \
  -H "Content-Type: application/json" \
  -d '{
    "ruleId": "rule-sql-injection-001",
    "name": "SQL Injection Detection",
    "description": "Detects SQL injection attack patterns",
    "category": "INJECTION",
    "severity": "CRITICAL",
    "enabled": true
  }')
echo "$RULE_RESPONSE" | jq .
echo ""

# Test 2: Ingest a high-severity event with admin path (threat score should be high)
echo "2️⃣  Ingesting event with /admin path (threat score calculation test)..."
EVENT_RESPONSE=$(curl -s -X POST "$API_BASE/v1/events/ingest" \
  -H "Content-Type: application/json" \
  -d '{
    "clientIp": "192.168.1.100",
    "path": "/api/admin/dashboard",
    "httpMethod": "GET",
    "action": "DENY",
    "ruleId": "rule-sql-injection-001",
    "timestamp": '$(date +%s)000'
  }')
echo "$EVENT_RESPONSE" | jq .
echo ""

# Test 3: Ingest event with /login path (should add path bonus)
echo "3️⃣  Ingesting event with /login path..."
EVENT_RESPONSE=$(curl -s -X POST "$API_BASE/v1/events/ingest" \
  -H "Content-Type: application/json" \
  -d '{
    "clientIp": "10.0.0.50",
    "path": "/user/login",
    "httpMethod": "POST",
    "action": "ALERT",
    "payload": "{\"username\":\"admin\"}",
    "ruleId": "rule-sql-injection-001",
    "timestamp": '$(date +%s)000'
  }')
echo "$EVENT_RESPONSE" | jq .
echo ""

# Test 4: Test batch ingestion
echo "4️⃣  Batch ingesting multiple events..."
BATCH_RESPONSE=$(curl -s -X POST "$API_BASE/v1/events/ingest/batch" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "clientIp": "192.168.1.100",
      "path": "/api/search",
      "httpMethod": "POST",
      "action": "MONITOR",
      "ruleId": "rule-sql-injection-001",
      "timestamp": '$(date +%s)000'
    },
    {
      "clientIp": "192.168.1.100",
      "path": "/api/data",
      "httpMethod": "GET",
      "action": "MONITOR",
      "ruleId": "rule-sql-injection-001",
      "timestamp": '$(date +%s)000'
    }
  ]')
echo "$BATCH_RESPONSE" | jq .
echo ""

# Test 5: Retrieve created rule
echo "5️⃣  Retrieving rule details..."
RULE_GET=$(curl -s -X GET "$API_BASE/v1/rules/rule-sql-injection-001")
echo "$RULE_GET" | jq .
echo ""

# Test 6: Get all rules
echo "6️⃣  Listing all rules..."
RULES_LIST=$(curl -s -X GET "$API_BASE/v1/rules")
echo "$RULES_LIST" | jq .
echo ""

echo "=========================================="
echo "✅ Test Complete!"
echo "=========================================="

