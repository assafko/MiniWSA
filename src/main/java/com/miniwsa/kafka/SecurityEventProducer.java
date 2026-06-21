package com.miniwsa.kafka;

import com.miniwsa.dto.SecurityEventRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

/**
 * Producer service for sending SecurityEventRequests to Kafka topic.
 * Handles both single and batch event ingestion requests.
 */
@Slf4j
@Service
public class SecurityEventProducer {

    private final KafkaTemplate<String, SecurityEventRequest> kafkaTemplate;
    private final String securityEventsTopic;

    public SecurityEventProducer(KafkaTemplate<String, SecurityEventRequest> kafkaTemplate,
                                @Value("${kafka.topics.security-events}") String securityEventsTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.securityEventsTopic = securityEventsTopic;
    }

    /**
     * Send a single security event request to Kafka topic.
     *
     * @param request the security event request to send
     */
    public void sendEvent(SecurityEventRequest request) {
        log.debug("Sending security event request to Kafka topic: {}", request.getEventId());

        Message<SecurityEventRequest> message = MessageBuilder
                .withPayload(request)
                .setHeader(KafkaHeaders.TOPIC, securityEventsTopic)
                .setHeader("kafka_messageKey", request.getEventId())
                .build();

        kafkaTemplate.send(message)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.debug("Successfully sent event {} to Kafka", request.getEventId());
                    } else {
                        log.error("Failed to send event {} to Kafka: {}", request.getEventId(), ex.getMessage());
                    }
                });
    }

    /**
     * Send multiple security event requests to Kafka topic.
     *
     * @param requests list of security event requests to send
     */
    public void sendEventBatch(java.util.List<SecurityEventRequest> requests) {
        log.debug("Sending batch of {} security event requests to Kafka", requests.size());

        requests.forEach(this::sendEvent);

        log.debug("Batch of {} events sent to Kafka", requests.size());
    }
}


