package com.miniwsa.it;

import com.miniwsa.domain.entity.Rule;
import com.miniwsa.domain.entity.SecurityEvent;
import com.miniwsa.domain.enums.Action;
import com.miniwsa.domain.enums.RuleCategory;
import com.miniwsa.domain.enums.Severity;
import com.miniwsa.repository.RuleRepository;
import com.miniwsa.repository.SecurityEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable(named = "RUN_INTEGRATION_TESTS", matches = "true")
class SamplesApiIT extends BaseIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired RuleRepository ruleRepository;
    @Autowired SecurityEventRepository eventRepository;

    @Test
    void samplesReturnsNewestFirstAndSupportsPaging() throws Exception {
        Rule rule = ruleRepository.save(Rule.builder()
                .ruleId("rule-it-samples")
                .name("Samples Rule")
                .description("samples")
                .category(RuleCategory.BOT)
                .severity(Severity.LOW)
                .enabled(true)
                .build());

        long t1 = System.currentTimeMillis();
        long t2 = t1 + 1000;

        eventRepository.save(SecurityEvent.builder()
                .clientIp("1.1.1.1")
                .path("/a")
                .httpMethod("GET")
                .action(Action.ALERT)
                .rule(rule)
                .timestamp(t1)
                .receivedAt(t1)
                .attackType("BOT")
                .threatScore(10)
                .eventId("e-it-old")
                .configId(9L)
                .build());

        eventRepository.save(SecurityEvent.builder()
                .clientIp("2.2.2.2")
                .path("/b")
                .httpMethod("GET")
                .action(Action.DENY)
                .rule(rule)
                .timestamp(t2)
                .receivedAt(t2)
                .attackType("BOT")
                .threatScore(20)
                .eventId("e-it-new")
                .configId(9L)
                .build());

        mockMvc.perform(get("/api/v1/events/samples")
                                                .servletPath("/api")
                                                .param("limit", "1")
                        .param("configId", "9")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].eventId").value("e-it-new"));

        mockMvc.perform(get("/api/v1/events/samples")
                                                .servletPath("/api")
                                                .param("limit", "1")
                        .param("configId", "9")
                        .param("offset", "1"))
                .andExpect(status().isOk());
    }
}
