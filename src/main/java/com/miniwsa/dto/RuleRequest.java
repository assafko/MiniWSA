package com.miniwsa.dto;

import com.miniwsa.domain.enums.RuleCategory;
import com.miniwsa.domain.enums.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RuleRequest {
    @NotBlank(message = "ruleId is required")
    private String ruleId;

    @NotBlank(message = "name is required")
    private String name;

    private String description;

    @NotNull(message = "category is required")
    private RuleCategory category;

    @NotNull(message = "severity is required")
    private Severity severity;

    @Builder.Default
    private boolean enabled = true;
}

