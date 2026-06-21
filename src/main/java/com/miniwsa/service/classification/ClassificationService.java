package com.miniwsa.service.classification;

import com.miniwsa.domain.enums.RuleCategory;
import org.springframework.stereotype.Service;

@Service
public class ClassificationService {

    /**
     * Maps a RuleCategory to a human-readable attack type string.
     *
     * @param category the rule category
     * @return the human-readable attack type
     */
    public String classifyAttackType(RuleCategory category) {
        if (category == null) {
            return "Unknown";
        }
        return category.getDisplayName();
    }
}

