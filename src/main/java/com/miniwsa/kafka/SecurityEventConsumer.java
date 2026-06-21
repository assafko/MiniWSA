package com.miniwsa.kafka;

import com.miniwsa.dto.SecurityEventRequest;
import com.miniwsa.service.EventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Consumer service for processing SecurityEventRequests from Kafka topic.
 * Listens to security-events-topic and processes incoming events.
 */
@Slf4j
@Service
public class SecurityEventConsumer {

    private final EventService eventService;

    public SecurityEventConsumer(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * Listen for security event messages on the Kafka topic and process them.
     * This method is called automatically when messages are available on the topic.
     *
     * @param request the security event request from Kafka
     */
    @KafkaListener(topics = "${kafka.topics.security-events}",
                   groupId = "${spring.kafka.consumer.group-id}",
                   containerFactory = "kafkaListenerContainerFactory")
    public void consumeSecurityEvent(SecurityEventRequest request) {
        try {
            log.debug("Received security event from Kafka: {}", request.getEventId());

            // Process the event through the service layer
            eventService.ingestEvent(request);

            log.debug("Successfully processed event {} from Kafka", request.getEventId());
        } catch (Exception e) {
            log.error("Error processing security event {} from Kafka: {}",
                     request.getEventId(), e.getMessage(), e);
            // Note: Kafka will handle retries based on the consumer configuration
        }
    }
}

