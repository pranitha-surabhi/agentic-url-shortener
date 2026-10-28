package com.example.agentic.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ShortUrlResponse {

    private String id;

    private String originalUrl;

    private String shortCode;

    private String shortUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime expiresAt;

    private Long clickCount;

    private boolean active;
}