package com.miniwsa.dto;

import com.miniwsa.domain.enums.Action;
import com.miniwsa.domain.enums.RuleCategory;
import com.miniwsa.domain.enums.Severity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DtoSmokeTest {

    @Test
    void ruleRequest_builderAndDefaults() {
        RuleRequest req = RuleRequest.builder()
                .ruleId("R-1")
                .name("n")
                .category(RuleCategory.XSS)
                .severity(Severity.HIGH)
                .build();

        assertEquals("R-1", req.getRuleId());
        assertTrue(req.isEnabled(), "enabled should default to true");
    }

    @Test
    void securityEventRequest_builder() {
        SecurityEventRequest req = SecurityEventRequest.builder()
                .eventId("e1")
                .timestamp("2026-01-01T00:00:00Z")
                .clientIp("1.2.3.4")
                .path("/x")
                .method("GET")
                .action(Action.ALERT)
                .rule(RuleDTO.builder().id("R-1").build())
                .geoLocation(GeoLocationDTO.builder().country("US").city("NY").build())
                .build();

        assertEquals("e1", req.getEventId());
        assertEquals(Action.ALERT, req.getAction());
        assertEquals("R-1", req.getRule().getId());
        assertEquals("US", req.getGeoLocation().getCountry());
    }

    @Test
    void responseDtos_builder() {
        RuleResponse rr = RuleResponse.builder().id(1L).ruleId("R-1").name("n").build();
        assertEquals("R-1", rr.getRuleId());

        SecurityEventResponse er = SecurityEventResponse.builder()
                .id(2L)
                .ruleId("R-1")
                .severity(Severity.LOW)
                .build();
        assertEquals(2L, er.getId());
        assertEquals(Severity.LOW, er.getSeverity());
    }
}

