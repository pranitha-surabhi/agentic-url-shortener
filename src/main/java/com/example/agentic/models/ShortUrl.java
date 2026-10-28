package com.example.agentic.models;

import com.example.agentic.used.enums.UrlStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrl {

    private String id;

    private String originalUrl;

    private String shortCode;

    private UrlStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime expiresAt;

    private LocalDateTime lastAccessedAt;

    private Long clickCount;

    private boolean active;
}