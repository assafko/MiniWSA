package com.miniwsa.service;

import com.miniwsa.domain.entity.Rule;
import com.miniwsa.domain.entity.SecurityEvent;
import com.miniwsa.domain.enums.Action;
import com.miniwsa.domain.enums.RuleCategory;
import com.miniwsa.dto.SamplesResponse;
import com.miniwsa.dto.SecurityEventResponse;
import com.miniwsa.repository.SecurityEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class EventQueryService {

    private final SecurityEventRepository repository;

    public EventQueryService(SecurityEventRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public SamplesResponse getSamples(Long configId,
                                      String fromIso,
                                      String toIso,
                                      String categoryStr,
                                      String actionStr,
                                      Integer limit,
                                      Integer offset) {
        Long from = parseInstantOrNull(fromIso);
        Long to = parseInstantOrNull(toIso);
        if (from != null && to != null && from > to) {
            throw new IllegalArgumentException("from must be <= to");
        }
        RuleCategory category = parseEnumOrNull(categoryStr, RuleCategory.class);
        Action action = parseEnumOrNull(actionStr, Action.class);

        int effLimit = (limit == null ? 20 : Math.max(1, Math.min(100, limit)));
        int effOffset = (offset == null ? 0 : Math.max(0, offset));
        int page = effOffset / effLimit;

        Pageable pageable = PageRequest.of(page, effLimit, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<SecurityEvent> pageResult = repository.findSamples(configId, from, to, category, action, pageable);

        List<SecurityEventResponse> items = pageResult.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return SamplesResponse.builder()
                .total(pageResult.getTotalElements())
                .items(items)
                .build();
    }

    private Long parseInstantOrNull(String iso) {
        if (iso == null || iso.isBlank()) return null;
        try {
            return Instant.parse(iso).toEpochMilli();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date; must be ISO-8601");
        }
    }

    private <E extends Enum<E>> E parseEnumOrNull(String value, Class<E> enumType) {
        if (value == null || value.isBlank()) return null;
        try {
            return Enum.valueOf(enumType, value.trim().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid value for " + enumType.getSimpleName());
        }
    }

    private SecurityEventResponse mapToResponse(SecurityEvent event) {
        Rule rule = event.getRule();
        return SecurityEventResponse.builder()
                .id(event.getId())
                .clientIp(event.getClientIp())
                .path(event.getPath())
                .httpMethod(event.getHttpMethod())
                .action(event.getAction())
                .payload(event.getPayload())
                .ruleId(rule != null ? rule.getRuleId() : null)
                .ruleName(rule != null ? rule.getName() : null)
                .severity(rule != null ? rule.getSeverity() : null)
                .timestamp(event.getTimestamp())
                .receivedAt(event.getReceivedAt())
                .attackType(event.getAttackType())
                .threatScore(event.getThreatScore())
                .createdAt(event.getCreatedAt())
                .eventId(event.getEventId())
                .configId(event.getConfigId())
                .policyId(event.getPolicyId())
                .hostname(event.getHostname())
                .statusCode(event.getStatusCode())
                .userAgent(event.getUserAgent())
                .geoCountry(event.getGeoCountry())
                .geoCity(event.getGeoCity())
                .requestSize(event.getRequestSize())
                .responseSize(event.getResponseSize())
                .build();
    }
}
