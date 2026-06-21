package com.miniwsa.kafka;

import com.miniwsa.domain.enums.Action;
import com.miniwsa.dto.RuleDTO;
import com.miniwsa.dto.SecurityEventRequest;
import com.miniwsa.service.EventService;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.Mockito.*;

class SecurityEventConsumerTest {

    @Test
    void consumeSecurityEvent_callsIngestEvent() {
        EventService eventService = mock(EventService.class);
        SecurityEventConsumer consumer = new SecurityEventConsumer(eventService);

        SecurityEventRequest req = SecurityEventRequest.builder()
                .eventId("e1")
                .timestamp(Instant.now().toString())
                .clientIp("1.2.3.4")
                .path("/x")
                .method("GET")
                .action(Action.ALERT)
                .rule(RuleDTO.builder().id("R-1").build())
                .build();

        consumer.consumeSecurityEvent(req);

        verify(eventService).ingestEvent(req);
    }

    @Test
    void consumeSecurityEvent_whenServiceThrows_doesNotPropagate() {
        EventService eventService = mock(EventService.class);
        doThrow(new RuntimeException("boom")).when(eventService).ingestEvent(any());

        SecurityEventConsumer consumer = new SecurityEventConsumer(eventService);

        SecurityEventRequest req = SecurityEventRequest.builder()
                .eventId("e1")
                .timestamp(Instant.now().toString())
                .clientIp("1.2.3.4")
                .path("/x")
                .method("GET")
                .action(Action.ALERT)
                .rule(RuleDTO.builder().id("R-1").build())
                .build();

        consumer.consumeSecurityEvent(req);

        verify(eventService).ingestEvent(req);
    }
}

