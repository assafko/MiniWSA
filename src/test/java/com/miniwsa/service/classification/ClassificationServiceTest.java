package com.miniwsa.service.classification;

import com.miniwsa.domain.enums.RuleCategory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClassificationServiceTest {

    private final ClassificationService service = new ClassificationService();

    @Test
    void classifyAttackType_nullCategory_returnsUnknown() {
        assertEquals("Unknown", service.classifyAttackType(null));
    }

    @Test
    void classifyAttackType_nonNullCategory_returnsDisplayName() {
        assertEquals(RuleCategory.XSS.getDisplayName(), service.classifyAttackType(RuleCategory.XSS));
    }
}

