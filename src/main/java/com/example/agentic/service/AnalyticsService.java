package com.example.agentic.service;

import com.example.agentic.dto.response.AnalyticsResponse;
import com.example.agentic.exceptions.ResourceNotFoundException;
import com.example.agentic.models.ShortUrl;
import com.example.agentic.repository.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ShortUrlRepository repository;

    public AnalyticsResponse getAnalytics(String shortCode) {

        ShortUrl shortUrl = repository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Short URL not found"));

        return AnalyticsResponse.builder()
                .shortCode(shortUrl.getShortCode())
                .totalClicks(shortUrl.getClickCount())
                .createdAt(shortUrl.getCreatedAt())
                .lastAccessedAt(shortUrl.getLastAccessedAt())
                .expiresAt(shortUrl.getExpiresAt())
                .build();
    }

}