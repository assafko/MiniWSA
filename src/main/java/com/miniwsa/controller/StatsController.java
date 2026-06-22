package com.miniwsa.controller;

import com.miniwsa.dto.stats.StatsSummaryResponse;
import com.miniwsa.service.StatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/v1/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<StatsSummaryResponse> getSummary(
            @RequestParam(name = "configId", required = false) Long configId,
            @RequestParam(name = "from") String fromIso,
            @RequestParam(name = "to") String toIso
    ) {
        long from;
        long to;
        try {
            from = Instant.parse(fromIso).toEpochMilli();
            to = Instant.parse(toIso).toEpochMilli();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid from/to; must be ISO-8601");
        }
        if (from > to) {
            throw new IllegalArgumentException("from must be <= to");
        }
        return ResponseEntity.ok(statsService.getSummary(configId, from, to));
    }
}
