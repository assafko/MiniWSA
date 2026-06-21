package com.miniwsa;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.junit.jupiter.api.Assertions.*;

class MiniSecurityAnalyticsApplicationTest {

    @Test
    void applicationClass_isAnnotatedAndLoadable() {
        assertNotNull(MiniSecurityAnalyticsApplication.class.getAnnotation(SpringBootApplication.class));
    }
}
