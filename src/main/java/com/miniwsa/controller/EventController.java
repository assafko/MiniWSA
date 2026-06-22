package com.miniwsa.controller;

import com.miniwsa.dto.SecurityEventRequest;
import com.miniwsa.kafka.SecurityEventProducer;
import com.miniwsa.service.EventService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for security event ingestion and retrieval.
 * Events are sent to Kafka topic for asynchronous processing to handle burst traffic.
 */
@Slf4j
@RestController
@RequestMapping("/v1/events")
public class EventController {

    private final EventService eventService;
    private final SecurityEventProducer securityEventProducer;

    public EventController(EventService eventService, SecurityEventProducer securityEventProducer) {
        this.eventService = eventService;
        this.securityEventProducer = securityEventProducer;
    }

    /**
     * Ingest a single security event asynchronously via Kafka.
     *
     * @param request the security event request
     * @return 201 Created indicating the event has been queued for processing
     */
    @PostMapping("/ingest")
    public ResponseEntity<String> ingestEvent(@Valid @RequestBody SecurityEventRequest request) {
        log.info("Received ingest request for event: {}", request.getEventId());

        // Send event to Kafka for asynchronous processing
        securityEventProducer.sendEvent(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Event queued for processing: " + request.getEventId());
    }

    /**
     * Ingest multiple security events in batch asynchronously via Kafka.
     *
     * @param requests list of security event requests
     * @return 201 Created indicating all events have been queued for processing
     */
    @PostMapping("/ingest/batch")
    public ResponseEntity<String> ingestBatchEvents(
            @Valid @RequestBody List<SecurityEventRequest> requests) {
        log.info("Received batch ingest request for {} events", requests.size());

        // Send all events to Kafka for asynchronous processing
        securityEventProducer.sendEventBatch(requests);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Batch of " + requests.size() + " events queued for processing");
    }
}

