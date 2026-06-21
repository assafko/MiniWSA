package com.miniwsa.service.classification;

import com.miniwsa.domain.entity.Rule;
import com.miniwsa.domain.entity.SecurityEvent;
import com.miniwsa.domain.enums.Action;
import com.miniwsa.domain.enums.RuleCategory;
import com.miniwsa.domain.enums.Severity;
import com.miniwsa.dto.RuleDTO;
import com.miniwsa.dto.SecurityEventRequest;
import com.miniwsa.repository.RuleRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EventEnrichmentServiceTest {

    @Test
    void enrichEvent_missingRuleId_throws() {
        ClassificationService classificationService = mock(ClassificationService.class);
        ThreatScoreCalculator threatScoreCalculator = mock(ThreatScoreCalculator.class);
        RuleRepository ruleRepository = mock(RuleRepository.class);

        EventEnrichmentService service = new EventEnrichmentService(classificationService, threatScoreCalculator, ruleRepository);

        SecurityEventRequest req = SecurityEventRequest.builder()
                .eventId("e1")
                .timestamp(Instant.now().toString())
                .clientIp("1.2.3.4")
                .path("/")
                .method("GET")
                .action(Action.MONITOR)
                .rule(null)
                .build();

        assertThrows(IllegalArgumentException.class, () -> service.enrichEvent(req));
    }

    @Test
    void enrichEvent_happyPath_mapsFields() {
        ClassificationService classificationService = mock(ClassificationService.class);
        ThreatScoreCalculator threatScoreCalculator = mock(ThreatScoreCalculator.class);
        RuleRepository ruleRepository = mock(RuleRepository.class);

        EventEnrichmentService service = new EventEnrichmentService(classificationService, threatScoreCalculator, ruleRepository);

        Rule rule = Rule.builder()
                .id(10L)
                .ruleId("R-1")
                .name("Rule")
                .category(RuleCategory.XSS)
                .severity(Severity.HIGH)
                .enabled(true)
                .build();

        when(ruleRepository.findByRuleId("R-1")).thenReturn(Optional.of(rule));
        when(classificationService.classifyAttackType(RuleCategory.XSS)).thenReturn("Cross-Site Scripting");
        when(threatScoreCalculator.calculateThreatScore(eq(Severity.HIGH), eq(Action.ALERT), anyString(), anyString()))
                .thenReturn(55);

        SecurityEventRequest req = SecurityEventRequest.builder()
                .eventId("e1")
                .timestamp("2026-01-01T00:00:00Z")
                .clientIp("1.2.3.4")
                .path("/login")
                .method("GET")
                .action(Action.ALERT)
                .rule(RuleDTO.builder().id("R-1").build())
                .payload("p")
                .build();

        SecurityEvent enriched = service.enrichEvent(req);

        assertEquals("e1", enriched.getEventId());
        assertEquals("1.2.3.4", enriched.getClientIp());
        assertEquals("/login", enriched.getPath());
        assertEquals("GET", enriched.getHttpMethod());
        assertEquals(Action.ALERT, enriched.getAction());
        assertEquals("p", enriched.getPayload());
        assertSame(rule, enriched.getRule());
        assertEquals("Cross-Site Scripting", enriched.getAttackType());
        assertEquals(55, enriched.getThreatScore());
        assertNotNull(enriched.getReceivedAt());
        assertEquals(Instant.parse("2026-01-01T00:00:00Z").toEpochMilli(), enriched.getTimestamp());
    }
}

