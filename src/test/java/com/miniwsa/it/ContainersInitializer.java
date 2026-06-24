package com.miniwsa.it;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.test.util.TestPropertyValues;

public class ContainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        TestPropertyValues.of(
                "spring.datasource.url=" + BaseIntegrationTest.POSTGRES.getJdbcUrl(),
                "spring.datasource.username=" + BaseIntegrationTest.POSTGRES.getUsername(),
                "spring.datasource.password=" + BaseIntegrationTest.POSTGRES.getPassword(),
                "spring.kafka.bootstrap-servers=" + BaseIntegrationTest.KAFKA.getBootstrapServers(),
                "spring.sql.init.mode=never",
                "spring.jpa.hibernate.ddl-auto=create"
        ).applyTo(applicationContext.getEnvironment());
    }
}
