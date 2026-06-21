package com.miniwsa.dto;

import com.miniwsa.domain.enums.RuleCategory;
import com.miniwsa.domain.enums.Severity;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RuleResponse {
    private Long id;
    private String ruleId;
    private String name;
    private String description;
    private RuleCategory category;
    private Severity severity;
    private boolean enabled;
    private Long createdAt;
    private Long updatedAt;
}

