package com.miniwsa.service;

import com.miniwsa.domain.entity.Rule;
import com.miniwsa.domain.entity.SecurityEvent;
import com.miniwsa.domain.enums.Action;
import com.miniwsa.domain.enums.RuleCategory;
import com.miniwsa.dto.SamplesResponse;
import com.miniwsa.repository.SecurityEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class EventQueryServiceTest {

    private SecurityEventRepository repository;
    private EventQueryService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(SecurityEventRepository.class);
        service = new EventQueryService(repository);
    }

    @Test
    void mapsEntitiesAndPaginates() {
        Rule rule = Rule.builder().ruleId("R-1").name("Rule 1").build();
        SecurityEvent e = SecurityEvent.builder()
                .id(1L)
                .clientIp("1.2.3.4")
                .path("/x")
                .httpMethod("GET")
                .action(Action.ALERT)
                .rule(rule)
                .timestamp(1000L)
                .receivedAt(2000L)
                .attackType("X")
                .threatScore(42)
                .build();
        Page<SecurityEvent> page = new PageImpl<>(List.of(e), PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "timestamp")), 1);
        when(repository.findSamples(any(), any(), any(), any(), any(), any())).thenReturn(page);

        SamplesResponse resp = service.getSamples(null, null, null, null, null, 20, 0);
        assertThat(resp.getTotal()).isEqualTo(1);
        assertThat(resp.getItems()).hasSize(1);
        assertThat(resp.getItems().get(0).getClientIp()).isEqualTo("1.2.3.4");
    }
}
