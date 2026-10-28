package com.example.agentic.controller;

import com.example.agentic.used.constants.ApiConstants;
import com.example.agentic.used.response.ApiResponse;
import com.example.agentic.dto.response.AnalyticsResponse;
import com.example.agentic.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/{shortCode}")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> analytics(
            @PathVariable String shortCode) {

        AnalyticsResponse response =
                analyticsService.getAnalytics(shortCode);

        return ResponseEntity.ok(
                ApiResponse.<AnalyticsResponse>builder()
                        .success(true)
                        .message(ApiConstants.SUCCESS)
                        .data(response)
                        .build());
    }

}