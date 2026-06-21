package com.miniwsa.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniwsa.domain.enums.Action;
import com.miniwsa.dto.RuleDTO;
import com.miniwsa.dto.SecurityEventRequest;
import com.miniwsa.kafka.SecurityEventProducer;
import com.miniwsa.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private EventService eventService;
    @MockBean private SecurityEventProducer securityEventProducer;

    @Test
    void ingestEvent_returns202_andSendsToKafka() throws Exception {
        SecurityEventRequest req = SecurityEventRequest.builder()
                .eventId("e1")
                .timestamp(Instant.now().toString())
                .clientIp("1.2.3.4")
                .path("/x")
                .method("GET")
                .action(Action.ALERT)
                .rule(RuleDTO.builder().id("R-1").build())
                .build();

        mockMvc.perform(post("/v1/events/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isAccepted())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Event queued for processing")));

        verify(securityEventProducer).sendEvent(any(SecurityEventRequest.class));
        verifyNoInteractions(eventService);
    }
}

