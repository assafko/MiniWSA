package com.miniwsa.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RuleDTO {
    private String id;
    private String name;
    private String message;
    private String severity;
    private String category;
}

