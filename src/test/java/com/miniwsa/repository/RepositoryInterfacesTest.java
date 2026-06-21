package com.miniwsa.repository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryInterfacesTest {

    @Test
    void repositories_areInterfaces() {
        assertTrue(RuleRepository.class.isInterface());
        assertTrue(SecurityEventRepository.class.isInterface());
    }
}

