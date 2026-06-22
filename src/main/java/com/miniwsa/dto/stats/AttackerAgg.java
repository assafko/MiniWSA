package com.miniwsa.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttackerAgg {
    private String clientIp;
    private long count;
    private double avgThreatScore;
}
