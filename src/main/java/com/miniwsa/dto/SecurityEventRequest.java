package com.miniwsa.dto;

import com.miniwsa.domain.enums.Action;
import com.miniwsa.domain.enums.RuleCategory;
import com.miniwsa.domain.enums.Severity;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SecurityEventRequest {
    @NotBlank(message = "clientIp is required")
    private String clientIp;

    @NotBlank(message = "path is required")
    private String path;

    @NotBlank(message = "httpMethod is required")
    private String httpMethod;

    @NotNull(message = "action is required")
    private Action action;

    private String payload;

    @NotBlank(message = "ruleId is required")
    private String ruleId;

    @NotNull(message = "timestamp is required")
    private Long timestamp;
}

