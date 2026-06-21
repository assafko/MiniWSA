package com.miniwsa.service.classification;

import com.miniwsa.domain.entity.Rule;
import com.miniwsa.domain.entity.SecurityEvent;
import com.miniwsa.dto.SecurityEventRequest;
import com.miniwsa.repository.RuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates event enrichment by:
 * 1. Classifying the attack type based on rule category
 * 2. Computing threat score based on multiple factors
 * 3. Setting receivedAt timestamp server-side
 * 4. Returning enriched event ready for persistence
 */
@Service
public class EventEnrichmentService {

    private final ClassificationService classificationService;
    private final ThreatScoreCalculator threatScoreCalculator;
    private final RuleRepository ruleRepository;

    public EventEnrichmentService(ClassificationService classificationService,
                                   ThreatScoreCalculator threatScoreCalculator,
                                   RuleRepository ruleRepository) {
        this.classificationService = classificationService;
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
        // Fetch the rule
        Rule rule = ruleRepository.findByRuleId(request.getRuleId())
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + request.getRuleId()));

        // Classify attack type
        String attackType = classificationService.classifyAttackType(rule.getCategory());

        // Calculate threat score
        Integer threatScore = threatScoreCalculator.calculateThreatScore(
                rule.getSeverity(),
                request.getAction(),
                request.getPath(),
                request.getClientIp()
        );

        // Set receivedAt timestamp server-side
        Long receivedAt = System.currentTimeMillis();

        // Build and return enriched event
        return SecurityEvent.builder()
                .clientIp(request.getClientIp())
                .path(request.getPath())
                .httpMethod(request.getHttpMethod())
                .action(request.getAction())
                .payload(request.getPayload())
                .rule(rule)
                .timestamp(request.getTimestamp())
                .receivedAt(receivedAt)
                .attackType(attackType)
                .threatScore(threatScore)
                .build();
    }
}

