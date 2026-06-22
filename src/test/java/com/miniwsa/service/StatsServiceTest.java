package com.miniwsa.service;

import com.miniwsa.dto.stats.StatsSummaryResponse;
import com.miniwsa.repository.SecurityEventRepository;
import com.miniwsa.repository.projection.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class StatsServiceTest {

    private SecurityEventRepository repository;
    private StatsService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(SecurityEventRepository.class);
        service = new StatsService(repository);
    }

    @Test
    void buildsSummaryFromProjections() {
        when(repository.countByTimeRangeAndConfig(anyLong(), anyLong(), any())).thenReturn(5L);

        CategoryStatsProjection cat = Mockito.mock(CategoryStatsProjection.class);
        when(cat.getCategory()).thenReturn(com.miniwsa.domain.enums.RuleCategory.INJECTION);
        when(cat.getCount()).thenReturn(3L);
        when(cat.getAvgThreatScore()).thenReturn(70.0);
        when(repository.statsByCategory(anyLong(), anyLong(), any())).thenReturn(List.of(cat));

        ActionCountProjection act = Mockito.mock(ActionCountProjection.class);
        when(act.getAction()).thenReturn(com.miniwsa.domain.enums.Action.DENY);
        when(act.getCount()).thenReturn(4L);
        when(repository.statsByAction(anyLong(), anyLong(), any())).thenReturn(List.of(act));

        TopAttackerProjection attacker = Mockito.mock(TopAttackerProjection.class);
        when(attacker.getClientIp()).thenReturn("203.0.113.42");
        when(attacker.getCount()).thenReturn(2L);
        when(attacker.getAvgThreatScore()).thenReturn(80.0);
        when(repository.topAttackers(anyLong(), anyLong(), any(), any())).thenReturn(List.of(attacker));

        TopPathProjection path = Mockito.mock(TopPathProjection.class);
        when(path.getPath()).thenReturn("/api/v1/login");
        when(path.getCount()).thenReturn(2L);
        when(repository.topPaths(anyLong(), anyLong(), any(), any())).thenReturn(List.of(path));

        StatsSummaryResponse resp = service.getSummary(14227L, 0, 1);
        assertThat(resp.getConfigId()).isEqualTo(14227L);
        assertThat(resp.getTotalEvents()).isEqualTo(5);
        assertThat(resp.getByCategory().get("INJECTION").getCount()).isEqualTo(3);
        assertThat(resp.getByAction().get("DENY")).isEqualTo(4L);
        assertThat(resp.getTopAttackers()).hasSize(1);
        assertThat(resp.getTopTargetedPaths()).hasSize(1);
    }
}
