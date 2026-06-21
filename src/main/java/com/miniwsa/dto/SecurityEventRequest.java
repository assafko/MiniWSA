package com.miniwsa.dto;

import com.miniwsa.domain.enums.Action;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SecurityEventRequest {
    @NotBlank(message = "eventId is required")
    private String eventId;

    /** ISO-8601 timestamp string, e.g. 2026-05-20T14:32:10Z */
    @NotBlank(message = "timestamp is required")
    private String timestamp;

    private Long configId;

    private String policyId;

    @NotBlank(message = "clientIp is required")
    private String clientIp;

    private String hostname;

    @NotBlank(message = "path is required")
    private String path;

    @NotBlank(message = "method is required")
    private String method;

    private Integer statusCode;

    private String userAgent;

    @NotNull(message = "action is required")
    private Action action;

    private RuleDTO rule;

    private GeoLocationDTO geoLocation;

    private Integer requestSize;

    private Integer responseSize;

    private String payload;
}

