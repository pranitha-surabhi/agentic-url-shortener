package com.example.agentic.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClickAnalytics {

    private String shortCode;

    private String ipAddress;

    private String userAgent;

    private LocalDateTime clickedAt;
}