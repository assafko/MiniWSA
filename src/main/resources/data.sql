-- Sample rules for testing event ingestion
INSERT INTO rules (rule_id, name, description, category, severity, enabled, created_at, updated_at)
VALUES
  ('rule-sql-injection-001', 'SQL Injection Detection', 'Detects common SQL injection patterns', 'INJECTION', 'CRITICAL', true, 1718901200000, 1718901200000),
  ('rule-xss-001', 'Cross-Site Scripting Detection', 'Detects XSS attack patterns in parameters', 'XSS', 'HIGH', true, 1718901200000, 1718901200000),
  ('rule-dos-001', 'Denial of Service Detection', 'Detects potential DoS attacks', 'DOS', 'HIGH', true, 1718901200000, 1718901200000),
  ('rule-bot-001', 'Bot Activity Detection', 'Detects suspicious bot patterns', 'BOT', 'MEDIUM', true, 1718901200000, 1718901200000),
  ('rule-data-leakage-001', 'Data Leakage Detection', 'Detects potential data exfiltration', 'DATA_LEAKAGE', 'CRITICAL', true, 1718901200000, 1718901200000),
  ('rule-rate-limit-001', 'Rate Limiting Rule', 'Enforces rate limiting policies', 'RATE_LIMIT', 'MEDIUM', true, 1718901200000, 1718901200000),
  ('rule-protocol-001', 'Protocol Anomaly Detection', 'Detects protocol violations', 'PROTOCOL_VIOLATION', 'LOW', true, 1718901200000, 1718901200000)
ON CONFLICT (rule_id) DO NOTHING;
