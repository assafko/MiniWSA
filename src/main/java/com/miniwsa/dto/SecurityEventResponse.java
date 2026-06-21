package com.miniwsa.dto;

import com.miniwsa.domain.enums.Action;
import com.miniwsa.domain.enums.Severity;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SecurityEventResponse {
    private Long id;
    private String clientIp;
    private String path;
    private String httpMethod;
    private Action action;
    private String payload;
    private String ruleId;
    private String ruleName;
    private Severity severity;
    private Long timestamp;
    private Long receivedAt;
    private String attackType;
    private Integer threatScore;
    private Long createdAt;
}

