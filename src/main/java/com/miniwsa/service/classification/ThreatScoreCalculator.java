package com.miniwsa.service.classification;

import com.miniwsa.domain.entity.SecurityEvent;
import com.miniwsa.domain.enums.Action;
import com.miniwsa.domain.enums.Severity;
import com.miniwsa.repository.SecurityEventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Calculates threat score (0-100) based on multiple factors:
 * - Base score from severity (CRITICAL=40, HIGH=30, MEDIUM=20, LOW=10)
 * - Action bonus (DENY=+20, ALERT=+10, MONITOR=+0)
 * - Path pattern detection (+15 for /admin or /login)
 * - Repeat offender check (+15 if >5 events from same IP in last 10 minutes)
 * - Final score capped at 100
 */
@Service
public class ThreatScoreCalculator {

    private final SecurityEventRepository securityEventRepository;

    public ThreatScoreCalculator(SecurityEventRepository securityEventRepository) {
        this.securityEventRepository = securityEventRepository;
    }

    /**
     * Calculate the threat score for a security event.
     *
     * @param severity the rule severity
     * @param action the action taken
     * @param path the request path
     * @param clientIp the client IP address
     * @return threat score between 0 and 100
     */
    public Integer calculateThreatScore(Severity severity, Action action, String path, String clientIp) {
        int score = 0;

        // 1. Base score from severity
        score += severity.getBaseScore();

        // 2. Action bonus
        score += getActionBonus(action);

        // 3. Path pattern detection (+15 for /admin or /login)
        if (isAdminOrLoginPath(path)) {
            score += 15;
        }

        // 4. Repeat offender check (+15 if >5 events from same IP in last 10 minutes)
        if (isRepeatOffender(clientIp)) {
            score += 15;
        }

        // Cap at 100
        return Math.min(score, 100);
    }

    /**
     * Get the action bonus points.
     *
     * @param action the action type
     * @return bonus points
     */
    private int getActionBonus(Action action) {
        return switch (action) {
            case DENY -> 20;
            case ALERT -> 10;
            case MONITOR -> 0;
        };
    }

    /**
     * Check if the path contains /admin or /login.
     *
     * @param path the request path
     * @return true if path contains /admin or /login
     */
    private boolean isAdminOrLoginPath(String path) {
        if (path == null) {
            return false;
        }
        String lowerPath = path.toLowerCase();
        return lowerPath.contains("/admin") || lowerPath.contains("/login");
    }

    /**
     * Check if the client IP is a repeat offender (>5 events in last 10 minutes).
     *
     * @param clientIp the client IP address
     * @return true if repeat offender
     */
    private boolean isRepeatOffender(String clientIp) {
        // 10 minutes in milliseconds
        long tenMinutesAgo = System.currentTimeMillis() - (10 * 60 * 1000);

        List<SecurityEvent> recentEvents = securityEventRepository.findRecentEventsByClientIp(
                clientIp,
                tenMinutesAgo
        );

        // Return true if more than 5 events found
        return recentEvents.size() > 5;
    }
}

