package com.miniwsa.domain.enums;

public enum Severity {
    CRITICAL(40),
    HIGH(30),
    MEDIUM(20),
    LOW(10);

    private final int baseScore;

    Severity(int baseScore) {
        this.baseScore = baseScore;
    }

    public int getBaseScore() {
        return baseScore;
    }
}

