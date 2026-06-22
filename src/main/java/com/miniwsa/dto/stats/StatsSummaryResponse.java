package com.miniwsa.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatsSummaryResponse {
    private Long configId; // nullable when aggregated across all configurations
    private TimeRangeDTO timeRange;
    private long totalEvents;

    // Keyed by category name (e.g., INJECTION, BOT)
    private Map<String, CategoryAggSummary> byCategory;

    // Keyed by action name (DENY, ALERT, MONITOR)
    private Map<String, Long> byAction;

    private List<AttackerAgg> topAttackers;
    private List<PathAgg> topTargetedPaths;
}
