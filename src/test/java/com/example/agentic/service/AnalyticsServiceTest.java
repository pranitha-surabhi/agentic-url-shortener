package com.example.agentic.service;

import com.example.agentic.dto.response.AnalyticsResponse;
import com.example.agentic.models.ShortUrl;
import com.example.agentic.repository.ShortUrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private ShortUrlRepository repository;

    private AnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new AnalyticsService(repository);
    }

    @Test
    void shouldReturnAnalytics() {

        ShortUrl shortUrl = ShortUrl.builder()
                .shortCode("ABC123")
                .clickCount(5L)
                .createdAt(LocalDateTime.now().minusHours(2))
                .lastAccessedAt(LocalDateTime.now())
                .expiresAt(null)
                .build();

        when(repository.findByShortCode("ABC123"))
                .thenReturn(Optional.of(shortUrl));

        AnalyticsResponse response =
                service.getAnalytics("ABC123");

        assertNotNull(response);
        assertEquals("ABC123", response.getShortCode());
        assertEquals(5L, response.getTotalClicks());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getLastAccessedAt());
    }

    @Test
    void shouldThrowExceptionWhenUrlDoesNotExist() {

        when(repository.findByShortCode("INVALID"))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> service.getAnalytics("INVALID")
        );
    }
}