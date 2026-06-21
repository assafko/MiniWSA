package com.miniwsa.domain.entity;

import com.miniwsa.domain.enums.Action;
import com.miniwsa.domain.enums.RuleCategory;
import com.miniwsa.domain.enums.Severity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntityLifecycleTest {

    @Test
    void rule_onCreate_setsTimestamps() {
        Rule rule = Rule.builder()
                .ruleId("R-1")
                .name("n")
                .category(RuleCategory.XSS)
                .severity(Severity.LOW)
                .enabled(true)
                .build();

        assertNull(rule.getCreatedAt());
        assertNull(rule.getUpdatedAt());

        rule.onCreate();

        assertNotNull(rule.getCreatedAt());
        assertNotNull(rule.getUpdatedAt());
        assertTrue(rule.getUpdatedAt() >= rule.getCreatedAt());
    }

    @Test
    void rule_onUpdate_updatesUpdatedAt() throws Exception {
        Rule rule = Rule.builder()
                .ruleId("R-1")
                .name("n")
                .category(RuleCategory.XSS)
                .severity(Severity.LOW)
                .enabled(true)
                .build();

        rule.onCreate();
        Long created = rule.getCreatedAt();
        Long updated1 = rule.getUpdatedAt();

        Thread.sleep(2);
        rule.onUpdate();

        assertEquals(created, rule.getCreatedAt());
        assertTrue(rule.getUpdatedAt() >= updated1);
    }

    @Test
    void securityEvent_onCreate_setsCreatedAt_andReceivedAtIfMissing() {
        Rule rule = Rule.builder()
                .ruleId("R-1")
                .name("n")
                .category(RuleCategory.XSS)
                .severity(Severity.LOW)
                .enabled(true)
                .build();

        SecurityEvent event = SecurityEvent.builder()
                .clientIp("1.2.3.4")
                .path("/x")
                .httpMethod("GET")
                .action(Action.ALERT)
                .rule(rule)
                .timestamp(1L)
                .attackType("t")
                .threatScore(1)
                .receivedAt(null)
                .build();

        assertNull(event.getCreatedAt());
        assertNull(event.getReceivedAt());

        event.onCreate();

        assertNotNull(event.getCreatedAt());
        assertNotNull(event.getReceivedAt());
    }

    @Test
    void geoLocation_builder() {
        GeoLocation g = GeoLocation.builder().country("US").city("NY").build();
        assertEquals("US", g.getCountry());
        assertEquals("NY", g.getCity());
    }
}

