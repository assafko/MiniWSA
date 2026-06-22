package com.miniwsa.repository.projection;

import com.miniwsa.domain.enums.RuleCategory;

public interface CategoryStatsProjection {
    RuleCategory getCategory();
    long getCount();
    Double getAvgThreatScore();
}
