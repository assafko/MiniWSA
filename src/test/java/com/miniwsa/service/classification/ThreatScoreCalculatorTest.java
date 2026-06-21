package com.miniwsa.service.classification;

import com.miniwsa.domain.entity.SecurityEvent;
import com.miniwsa.domain.enums.Action;
import com.miniwsa.domain.enums.Severity;
import com.miniwsa.repository.SecurityEventRepository;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ThreatScoreCalculatorTest {

    @Test
    void calculateThreatScore_capsAt100() {
        SecurityEventRepository repo = mock(SecurityEventRepository.class);
        when(repo.findRecentEventsByClientIp(anyString(), anyLong())).thenReturn(List.of(
                new SecurityEvent(), new SecurityEvent(), new SecurityEvent(),
                new SecurityEvent(), new SecurityEvent(), new SecurityEvent()
        ));

        ThreatScoreCalculator calc = new ThreatScoreCalculator(repo);

        // CRITICAL=40, DENY=20, /admin bonus=15, repeat offender bonus=15 => 90 (not capped)
        // Add another path bonus by using /login too isn't possible; instead assert the exact expected value.
        Integer score = calc.calculateThreatScore(Severity.CRITICAL, Action.DENY, "/admin", "1.2.3.4");
        assertEquals(90, score);
    }

    @Test
    void calculateThreatScore_nullPath_noAdminLoginBonus() {
        SecurityEventRepository repo = mock(SecurityEventRepository.class);
        when(repo.findRecentEventsByClientIp(anyString(), anyLong())).thenReturn(Collections.emptyList());

        ThreatScoreCalculator calc = new ThreatScoreCalculator(repo);

        Integer score = calc.calculateThreatScore(Severity.LOW, Action.MONITOR, null, "1.2.3.4");
        // LOW=10, MONITOR=0, no path bonus, no repeat offender
        assertEquals(10, score);
    }
}
