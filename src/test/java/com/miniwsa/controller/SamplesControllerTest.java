package com.miniwsa.controller;

import com.miniwsa.dto.SamplesResponse;
import com.miniwsa.dto.SecurityEventResponse;
import com.miniwsa.service.EventQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SamplesController.class)
class SamplesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventQueryService eventQueryService;

    @Test
    void returns200WithDefaults() throws Exception {
        when(eventQueryService.getSamples(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(SamplesResponse.builder().total(0).items(List.of()).build());

        mockMvc.perform(get("/v1/events/samples")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void returns400OnInvalidDate() throws Exception {
        when(eventQueryService.getSamples(any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Invalid date; must be ISO-8601"));

        mockMvc.perform(get("/v1/events/samples")
                        .param("from", "not-a-date"))
                .andExpect(status().isBadRequest());
    }
}
