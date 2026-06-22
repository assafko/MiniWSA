package com.miniwsa.controller;

import com.miniwsa.dto.stats.StatsSummaryResponse;
import com.miniwsa.dto.stats.TimeRangeDTO;
import com.miniwsa.service.StatsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StatsController.class)
class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatsService statsService;

    @Test
    void returns200OnValidRequest() throws Exception {
        when(statsService.getSummary(any(), anyLong(), anyLong())).thenReturn(
                StatsSummaryResponse.builder()
                        .configId(123L)
                        .timeRange(TimeRangeDTO.builder().from("2024-01-01T00:00:00Z").to("2024-01-02T00:00:00Z").build())
                        .totalEvents(0)
                        .build()
        );
        mockMvc.perform(get("/v1/stats/summary")
                        .param("from", "2024-01-01T00:00:00Z")
                        .param("to", "2024-01-02T00:00:00Z"))
                .andExpect(status().isOk());
    }

    @Test
    void returns400OnInvalidDates() throws Exception {
        mockMvc.perform(get("/v1/stats/summary")
                        .param("from", "invalid")
                        .param("to", "2024-01-02T00:00:00Z"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/v1/stats/summary")
                        .param("from", "2024-01-03T00:00:00Z")
                        .param("to", "2024-01-02T00:00:00Z"))
                .andExpect(status().isBadRequest());
    }
}
