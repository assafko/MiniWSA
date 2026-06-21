package com.miniwsa.kafka;

import com.miniwsa.dto.SecurityEventRequest;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import static org.junit.jupiter.api.Assertions.*;

class KafkaConfigTest {

    @Test
    void kafkaBeans_canBeCreated() {
        KafkaConfig cfg = new KafkaConfig();

        // set @Value fields via reflection (unit test without Spring context)
        setField(cfg, "bootstrapServers", "localhost:9092");
        setField(cfg, "securityEventsTopic", "security-events-topic");
        setField(cfg, "consumerGroupId", "group");

        KafkaAdmin admin = cfg.kafkaAdmin();
        assertNotNull(admin);

        NewTopic topic = cfg.securityEventsTopic();
        assertEquals("security-events-topic", topic.name());

        ProducerFactory<String, SecurityEventRequest> pf = cfg.producerFactory();
        assertNotNull(pf);

        KafkaTemplate<String, SecurityEventRequest> template = cfg.kafkaTemplate();
        assertNotNull(template);

        ConsumerFactory<String, SecurityEventRequest> cf = cfg.consumerFactory();
        assertNotNull(cf);

        KafkaListenerContainerFactory<?> lcf = cfg.kafkaListenerContainerFactory();
        assertNotNull(lcf);
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            var f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

