package com.miniwsa.service;

import com.miniwsa.domain.entity.Rule;
import com.miniwsa.dto.RuleRequest;
import com.miniwsa.dto.RuleResponse;
import com.miniwsa.repository.RuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing security rules.
 */
@Service
public class RuleService {

    private final RuleRepository ruleRepository;

    public RuleService(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    /**
     * Create a new security rule.
     *
     * @param request the rule request
     * @return the created rule response
     */
    @Transactional
    public RuleResponse createRule(RuleRequest request) {
        // Check if rule with same ruleId already exists
        if (ruleRepository.findByRuleId(request.getRuleId()).isPresent()) {
            throw new IllegalArgumentException("Rule with ID '" + request.getRuleId() + "' already exists");
        }

        Rule rule = Rule.builder()
                .ruleId(request.getRuleId())
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .severity(request.getSeverity())
                .enabled(request.isEnabled())
                .build();

        Rule savedRule = ruleRepository.save(rule);
        return mapToResponse(savedRule);
    }

    /**
     * Get a rule by its rule ID.
     *
     * @param ruleId the rule ID
     * @return the rule response
     */
    @Transactional(readOnly = true)
    public RuleResponse getRuleByRuleId(String ruleId) {
        Rule rule = ruleRepository.findByRuleId(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));
        return mapToResponse(rule);
    }

    /**
     * Get all rules.
     *
     * @return list of rule responses
     */
    @Transactional(readOnly = true)
    public List<RuleResponse> getAllRules() {
        return ruleRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Update a rule.
     *
     * @param ruleId the rule ID to update
     * @param request the updated rule data
     * @return the updated rule response
     */
    @Transactional
    public RuleResponse updateRule(String ruleId, RuleRequest request) {
        Rule rule = ruleRepository.findByRuleId(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));

        rule.setName(request.getName());
        rule.setDescription(request.getDescription());
        rule.setCategory(request.getCategory());
        rule.setSeverity(request.getSeverity());
        rule.setEnabled(request.isEnabled());

        Rule updatedRule = ruleRepository.save(rule);
        return mapToResponse(updatedRule);
    }

    /**
     * Delete a rule by its rule ID.
     *
     * @param ruleId the rule ID
     */
    @Transactional
    public void deleteRule(String ruleId) {
        Rule rule = ruleRepository.findByRuleId(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));
        ruleRepository.delete(rule);
    }

    /**
     * Convert Rule entity to RuleResponse DTO.
     *
     * @param rule the rule entity
     * @return the response DTO
     */
    private RuleResponse mapToResponse(Rule rule) {
        return RuleResponse.builder()
                .id(rule.getId())
                .ruleId(rule.getRuleId())
                .name(rule.getName())
                .description(rule.getDescription())
                .category(rule.getCategory())
                .severity(rule.getSeverity())
                .enabled(rule.isEnabled())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}

