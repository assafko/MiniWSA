package com.miniwsa.service;

import com.miniwsa.domain.entity.Rule;
import com.miniwsa.domain.enums.RuleCategory;
import com.miniwsa.domain.enums.Severity;
import com.miniwsa.dto.RuleRequest;
import com.miniwsa.dto.RuleResponse;
import com.miniwsa.repository.RuleRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RuleServiceTest {

    @Test
    void createRule_whenRuleIdExists_throws() {
        RuleRepository repo = mock(RuleRepository.class);
        when(repo.findByRuleId("R-1")).thenReturn(Optional.of(new Rule()));

        RuleService service = new RuleService(repo);

        RuleRequest req = RuleRequest.builder()
                .ruleId("R-1")
                .name("n")
                .category(RuleCategory.XSS)
                .severity(Severity.LOW)
                .enabled(true)
                .build();

        assertThrows(IllegalArgumentException.class, () -> service.createRule(req));
        verify(repo, never()).save(any());
    }

    @Test
    void getAllRules_mapsToResponses() {
        RuleRepository repo = mock(RuleRepository.class);
        RuleService service = new RuleService(repo);

        Rule r1 = Rule.builder()
                .id(1L)
                .ruleId("R-1")
                .name("Rule1")
                .description("d")
                .category(RuleCategory.XSS)
                .severity(Severity.HIGH)
                .enabled(true)
                .createdAt(1L)
                .updatedAt(2L)
                .build();

        when(repo.findAll()).thenReturn(List.of(r1));

        List<RuleResponse> resps = service.getAllRules();
        assertEquals(1, resps.size());
        assertEquals("R-1", resps.get(0).getRuleId());
        assertEquals("Rule1", resps.get(0).getName());
    }
}

