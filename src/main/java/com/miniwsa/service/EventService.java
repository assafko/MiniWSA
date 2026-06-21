package com.miniwsa.service;

import com.miniwsa.domain.entity.SecurityEvent;
import com.miniwsa.dto.SecurityEventRequest;
import com.miniwsa.dto.SecurityEventResponse;
import com.miniwsa.repository.SecurityEventRepository;
import com.miniwsa.service.classification.EventEnrichmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * High-level event service that orchestrates event ingestion and enrichment.
 */
@Service
public class EventService {

    private final SecurityEventRepository securityEventRepository;
    private final EventEnrichmentService eventEnrichmentService;

    public EventService(SecurityEventRepository securityEventRepository,
                        EventEnrichmentService eventEnrichmentService) {
        this.securityEventRepository = securityEventRepository;
        this.eventEnrichmentService = eventEnrichmentService;
    }

    /**
     * Ingest a single security event: enrich and save to database.
     *
     * @param request the incoming security event request
     * @return the persisted event as response
     */
    @Transactional
    public SecurityEventResponse ingestEvent(SecurityEventRequest request) {
        // Enrich the event
        SecurityEvent enrichedEvent = eventEnrichmentService.enrichEvent(request);

        // Save to database
        SecurityEvent savedEvent = securityEventRepository.save(enrichedEvent);

        // Convert to response DTO
        return mapToResponse(savedEvent);
    }

    /**
     * Ingest multiple security events: enrich and save each to database.
     *
     * @param requests list of incoming security event requests
     * @return list of persisted events as responses
     */
    @Transactional
    public List<SecurityEventResponse> ingestBatchEvents(List<SecurityEventRequest> requests) {
        return requests.stream()
                .map(this::ingestEvent)
                .toList();
    }

    /**
     * Convert SecurityEvent entity to response DTO.
     *
     * @param event the security event entity
     * @return the response DTO
     */
    private SecurityEventResponse mapToResponse(SecurityEvent event) {
        return SecurityEventResponse.builder()
                .id(event.getId())
                .clientIp(event.getClientIp())
                .path(event.getPath())
                .httpMethod(event.getHttpMethod())
                .action(event.getAction())
                .payload(event.getPayload())
                .ruleId(event.getRule().getRuleId())
                .ruleName(event.getRule().getName())
                .severity(event.getRule().getSeverity())
                .timestamp(event.getTimestamp())
                .receivedAt(event.getReceivedAt())
                .attackType(event.getAttackType())
                .threatScore(event.getThreatScore())
                .createdAt(event.getCreatedAt())
                .eventId(event.getEventId())
                .configId(event.getConfigId())
                .policyId(event.getPolicyId())
                .hostname(event.getHostname())
                .statusCode(event.getStatusCode())
                .userAgent(event.getUserAgent())
                .geoCountry(event.getGeoCountry())
                .geoCity(event.getGeoCity())
                .requestSize(event.getRequestSize())
                .responseSize(event.getResponseSize())
                .build();
    }
}

