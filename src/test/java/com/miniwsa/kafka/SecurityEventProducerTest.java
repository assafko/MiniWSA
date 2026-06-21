package com.miniwsa.kafka;

import com.miniwsa.dto.SecurityEventRequest;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SecurityEventProducerTest {

    @Test
    void sendEvent_sendsMessageToKafkaTemplate() {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, SecurityEventRequest> kafkaTemplate = mock(KafkaTemplate.class);
        when(kafkaTemplate.send(any(Message.class))).thenReturn(CompletableFuture.completedFuture(null));

        SecurityEventProducer producer = new SecurityEventProducer(kafkaTemplate, "topic");

        SecurityEventRequest req = SecurityEventRequest.builder().eventId("e1").build();
        producer.sendEvent(req);

        verify(kafkaTemplate).send(any(Message.class));
    }
}

