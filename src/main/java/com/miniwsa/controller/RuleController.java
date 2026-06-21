package com.miniwsa.controller;

import com.miniwsa.dto.RuleRequest;
import com.miniwsa.dto.RuleResponse;
import com.miniwsa.service.RuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for rule management.
 */
@RestController
@RequestMapping("/v1/rules")
public class RuleController {

    private final RuleService ruleService;

    public RuleController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    /**
     * Create a new security rule.
     *
     * @param request the rule request
     * @return 201 Created with the created rule
     */
    @PostMapping
    public ResponseEntity<RuleResponse> createRule(@Valid @RequestBody RuleRequest request) {
        RuleResponse response = ruleService.createRule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a rule by its rule ID.
     *
     * @param ruleId the rule ID
     * @return 200 OK with the rule
     */
    @GetMapping("/{ruleId}")
    public ResponseEntity<RuleResponse> getRuleByRuleId(@PathVariable String ruleId) {
        RuleResponse response = ruleService.getRuleByRuleId(ruleId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all rules.
     *
     * @return 200 OK with list of rules
     */
    @GetMapping
    public ResponseEntity<List<RuleResponse>> getAllRules() {
        List<RuleResponse> responses = ruleService.getAllRules();
        return ResponseEntity.ok(responses);
    }

    /**
     * Update a rule.
     *
     * @param ruleId the rule ID to update
     * @param request the updated rule data
     * @return 200 OK with the updated rule
     */
    @PutMapping("/{ruleId}")
    public ResponseEntity<RuleResponse> updateRule(@PathVariable String ruleId,
                                                    @Valid @RequestBody RuleRequest request) {
        RuleResponse response = ruleService.updateRule(ruleId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a rule by its rule ID.
     *
     * @param ruleId the rule ID
     * @return 204 No Content
     */
    @DeleteMapping("/{ruleId}")
    public ResponseEntity<Void> deleteRule(@PathVariable String ruleId) {
        ruleService.deleteRule(ruleId);
        return ResponseEntity.noContent().build();
    }
}

