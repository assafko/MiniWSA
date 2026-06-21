package com.miniwsa.controller;

import com.miniwsa.dto.SecurityEventRequest;
import com.miniwsa.dto.SecurityEventResponse;
import com.miniwsa.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for security event ingestion and retrieval.
 */
@RestController
@RequestMapping("/v1/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * Ingest a single security event.
     *
     * @param request the security event request
     * @return 201 Created with the enriched event
     */
    @PostMapping("/ingest")
    public ResponseEntity<SecurityEventResponse> ingestEvent(@Valid @RequestBody SecurityEventRequest request) {
        SecurityEventResponse response = eventService.ingestEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Ingest multiple security events in batch.
     *
     * @param requests list of security event requests
     * @return 201 Created with list of enriched events
     */
    @PostMapping("/ingest/batch")
    public ResponseEntity<List<SecurityEventResponse>> ingestBatchEvents(
            @Valid @RequestBody List<SecurityEventRequest> requests) {
        List<SecurityEventResponse> responses = eventService.ingestBatchEvents(requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }
}

