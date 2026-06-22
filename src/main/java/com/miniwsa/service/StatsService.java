package com.miniwsa.service;

import com.miniwsa.dto.stats.*;
import com.miniwsa.repository.SecurityEventRepository;
import com.miniwsa.repository.projection.ActionCountProjection;
import com.miniwsa.repository.projection.CategoryStatsProjection;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private final SecurityEventRepository securityEventRepository;

    public StatsService(SecurityEventRepository securityEventRepository) {
        this.securityEventRepository = securityEventRepository;
    }

    public StatsSummaryResponse getSummary(Long configId, long fromEpochMs, long toEpochMs) {
        long total = securityEventRepository.countByTimeRangeAndConfig(fromEpochMs, toEpochMs, configId);

        // byCategory
        Map<String, CategoryAggSummary> byCategory = new LinkedHashMap<>();
        for (CategoryStatsProjection p : securityEventRepository.statsByCategory(fromEpochMs, toEpochMs, configId)) {
            byCategory.put(p.getCategory().name(), CategoryAggSummary.builder()
                    .count(p.getCount())
                    .avgThreatScore(Optional.ofNullable(p.getAvgThreatScore()).orElse(0.0))
                    .build());
        }

        // byAction
        Map<String, Long> byAction = new LinkedHashMap<>();
        for (ActionCountProjection p : securityEventRepository.statsByAction(fromEpochMs, toEpochMs, configId)) {
            byAction.put(p.getAction().name(), p.getCount());
        }

        // topAttackers
        List<AttackerAgg> topAttackers = securityEventRepository
                .topAttackers(fromEpochMs, toEpochMs, configId, PageRequest.of(0, 10))
                .stream()
                .map(p -> AttackerAgg.builder()
                        .clientIp(p.getClientIp())
                        .count(p.getCount())
                        .avgThreatScore(Optional.ofNullable(p.getAvgThreatScore()).orElse(0.0))
                        .build())
                .collect(Collectors.toList());

        // topPaths
        List<PathAgg> topPaths = securityEventRepository
                .topPaths(fromEpochMs, toEpochMs, configId, PageRequest.of(0, 10))
                .stream()
                .map(p -> PathAgg.builder()
                        .path(p.getPath())
                        .count(p.getCount())
                        .build())
                .collect(Collectors.toList());

        return StatsSummaryResponse.builder()
                .configId(configId)
                .timeRange(TimeRangeDTO.builder()
                        .from(iso(fromEpochMs))
                        .to(iso(toEpochMs))
                        .build())
                .totalEvents(total)
                .byCategory(byCategory)
                .byAction(byAction)
                .topAttackers(topAttackers)
                .topTargetedPaths(topPaths)
                .build();
    }

    private String iso(long epochMs) {
        return java.time.Instant.ofEpochMilli(epochMs).toString();
    }
}
