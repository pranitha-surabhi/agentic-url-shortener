package com.example.agentic.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AnalyticsResponse {

    private String shortCode;

    private Long totalClicks;

    private LocalDateTime createdAt;

    private LocalDateTime lastAccessedAt;

    private LocalDateTime expiresAt;
}