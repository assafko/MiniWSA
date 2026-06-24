package com.miniwsa.service.classification;

import com.miniwsa.domain.entity.Rule;
import com.miniwsa.domain.entity.SecurityEvent;
import com.miniwsa.domain.enums.RuleCategory;
import com.miniwsa.dto.SecurityEventRequest;
import com.miniwsa.repository.RuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Orchestrates event enrichment by:
 * 1. Classifying the attack type based on rule category
 * 2. Computing threat score based on multiple factors
 * 3. Setting receivedAt timestamp server-side
 * 4. Returning enriched event ready for persistence
 */
@Service
public class EventEnrichmentService {

    private final ThreatScoreCalculator threatScoreCalculator;
    private final RuleRepository ruleRepository;

    public EventEnrichmentService(ThreatScoreCalculator threatScoreCalculator,
                                   RuleRepository ruleRepository) {
        this.threatScoreCalculator = threatScoreCalculator;
        this.ruleRepository = ruleRepository;
    }

    /**
     * Enrich a security event with classification and threat score.
     *
     * @param request the incoming security event request
     * @return enriched SecurityEvent entity ready for persistence
     * @throws IllegalArgumentException if rule not found
     */
    @Transactional(readOnly = true)
    public SecurityEvent enrichEvent(SecurityEventRequest request) {
        // Validate nested rule.id and fetch the rule
        if (request.getRule() == null || request.getRule().getId() == null) {
            throw new IllegalArgumentException("rule.id is required in the request");
        }
        Rule rule = ruleRepository.findByRuleId(request.getRule().getId())
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + request.getRule().getId()));

        // Classify attack type
        String attackType = classifyAttackType(rule.getCategory());

        // Parse provided timestamp (ISO-8601) to epoch millis; fall back to numeric parse or current time
        Long eventTimestamp;
        try {
            eventTimestamp = Instant.parse(request.getTimestamp()).toEpochMilli();
        } catch (Exception e) {
            try {
                eventTimestamp = Long.parseLong(request.getTimestamp());
            } catch (Exception ex) {
                eventTimestamp = System.currentTimeMillis();
            }
        }

        // Calculate threat score
        Integer threatScore = threatScoreCalculator.calculateThreatScore(
                rule.getSeverity(),
                request.getAction(),
                request.getPath(),
                request.getClientIp()
        );

        // Set receivedAt timestamp server-side
        Long receivedAt = System.currentTimeMillis();

        // Build and return enriched event mapping new fields
        return SecurityEvent.builder()
                .eventId(request.getEventId())
                .clientIp(request.getClientIp())
                .path(request.getPath())
                .httpMethod(request.getMethod())
                .action(request.getAction())
                .payload(request.getPayload())
                .rule(rule)
                .timestamp(eventTimestamp)
                .receivedAt(receivedAt)
                .attackType(attackType)
                .threatScore(threatScore)
                .configId(request.getConfigId())
                .policyId(request.getPolicyId())
                .hostname(request.getHostname())
                .statusCode(request.getStatusCode())
                .userAgent(request.getUserAgent())
                .geoCountry(request.getGeoLocation() != null ? request.getGeoLocation().getCountry() : null)
                .geoCity(request.getGeoLocation() != null ? request.getGeoLocation().getCity() : null)
                .requestSize(request.getRequestSize())
                .responseSize(request.getResponseSize())
                .build();
    }

    String classifyAttackType(RuleCategory category) {
        if (category == null) {
            return "Unknown";
        }
        return category.getDisplayName();
    }
}

