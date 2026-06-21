package com.miniwsa.domain.enums;

public enum RuleCategory {
    INJECTION("SQL/Command Injection"),
    XSS("Cross-Site Scripting"),
    PROTOCOL_VIOLATION("Protocol Anomaly"),
    DATA_LEAKAGE("Data Exfiltration"),
    BOT("Bot Activity"),
    DOS("Denial of Service"),
    RATE_LIMIT("Rate Limiting");

    private final String displayName;

    RuleCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

