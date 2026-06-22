package com.miniwsa.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeRangeDTO {
    private String from; // ISO8601 string
    private String to;   // ISO8601 string
}
