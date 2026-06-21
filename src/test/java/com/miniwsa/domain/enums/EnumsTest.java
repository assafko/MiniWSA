package com.miniwsa.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnumsTest {

    @Test
    void severity_baseScores() {
        assertEquals(40, Severity.CRITICAL.getBaseScore());
        assertEquals(30, Severity.HIGH.getBaseScore());
        assertEquals(20, Severity.MEDIUM.getBaseScore());
        assertEquals(10, Severity.LOW.getBaseScore());
    }

    @Test
    void ruleCategory_displayNameNotBlank() {
        for (RuleCategory c : RuleCategory.values()) {
            assertNotNull(c.getDisplayName());
            assertFalse(c.getDisplayName().isBlank());
        }
    }

    @Test
    void action_valuesPresent() {
        assertNotNull(Action.valueOf("DENY"));
        assertNotNull(Action.valueOf("ALERT"));
        assertNotNull(Action.valueOf("MONITOR"));
    }
}

