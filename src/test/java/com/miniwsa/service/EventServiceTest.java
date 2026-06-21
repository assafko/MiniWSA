package com.miniwsa.service;

import com.miniwsa.domain.entity.Rule;
import com.miniwsa.domain.entity.SecurityEvent;
import com.miniwsa.domain.enums.Action;
import com.miniwsa.domain.enums.RuleCategory;
import com.miniwsa.domain.enums.Severity;
import com.miniwsa.dto.RuleDTO;
import com.miniwsa.dto.SecurityEventRequest;
import com.miniwsa.dto.SecurityEventResponse;
import com.miniwsa.repository.SecurityEventRepository;
import com.miniwsa.service.classification.EventEnrichmentService;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EventServiceTest {

    @Test
    void ingestEvent_enrichesSavesAndMapsToResponse() {
        SecurityEventRepository repo = mock(SecurityEventRepository.class);
        EventEnrichmentService enrichmentService = mock(EventEnrichmentService.class);

        EventService service = new EventService(repo, enrichmentService);

        SecurityEventRequest req = SecurityEventRequest.builder()
                .eventId("e1")
                .timestamp(Instant.now().toString())
                .clientIp("1.2.3.4")
                .path("/x")
                .method("GET")
                .action(Action.ALERT)
                .rule(RuleDTO.builder().id("R-1").build())
                .build();

        Rule rule = Rule.builder()
                .id(1L)
                .ruleId("R-1")
                .name("Rule")
                .category(RuleCategory.XSS)
                .severity(Severity.HIGH)
                .enabled(true)
                .build();

        SecurityEvent enriched = SecurityEvent.builder()
                .id(99L)
                .eventId("e1")
                .clientIp("1.2.3.4")
                .path("/x")
                .httpMethod("GET")
                .action(Action.ALERT)
                .payload("p")
                .rule(rule)
                .timestamp(1L)
                .receivedAt(2L)
                .attackType("Cross-Site Scripting")
                .threatScore(42)
                .createdAt(3L)
                .build();

        when(enrichmentService.enrichEvent(any(SecurityEventRequest.class))).thenReturn(enriched);
        when(repo.save(enriched)).thenReturn(enriched);

        SecurityEventResponse resp = service.ingestEvent(req);

        assertEquals(99L, resp.getId());
        assertEquals("1.2.3.4", resp.getClientIp());
        assertEquals("/x", resp.getPath());
        assertEquals("GET", resp.getHttpMethod());
        assertEquals(Action.ALERT, resp.getAction());
        assertEquals("R-1", resp.getRuleId());
        assertEquals("Rule", resp.getRuleName());
        assertEquals(Severity.HIGH, resp.getSeverity());
        assertEquals("Cross-Site Scripting", resp.getAttackType());
        assertEquals(42, resp.getThreatScore());

        verify(enrichmentService).enrichEvent(req);
        verify(repo).save(enriched);
    }
}

