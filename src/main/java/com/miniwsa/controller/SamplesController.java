package com.miniwsa.controller;

import com.miniwsa.dto.SamplesResponse;
import com.miniwsa.service.EventQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/events")
@RequiredArgsConstructor
public class SamplesController {

    private final EventQueryService eventQueryService;

    @GetMapping("/samples")
    public ResponseEntity<SamplesResponse> getSamples(
            @RequestParam(value = "configId", required = false) Long configId,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "offset", required = false) Integer offset
    ) {
        return ResponseEntity.ok(eventQueryService.getSamples(configId, from, to, category, action, limit, offset));
    }
}
