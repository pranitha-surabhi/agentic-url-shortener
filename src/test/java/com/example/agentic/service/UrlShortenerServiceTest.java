package com.example.agentic.service;

import com.example.agentic.used.enums.UrlStatus;
import com.example.agentic.dto.request.CreateShortUrlRequest;
import com.example.agentic.dto.request.UpdateShortUrlRequest;
import com.example.agentic.dto.response.ShortUrlResponse;
import com.example.agentic.exceptions.InvalidUrlException;
import com.example.agentic.exceptions.ResourceNotFoundException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceTest {

    @Mock
    private ShortUrlRepository repository;

    private UrlShortenerService service;

    @BeforeEach
    void setUp() {
        service = new UrlShortenerService(repository);
    }

    @Test
    void shouldCreateShortUrl() {

        CreateShortUrlRequest request = new CreateShortUrlRequest();
        request.setOriginalUrl("https://spring.io");
        request.setExpiryInHours(24L);

        when(repository.existsByShortCode(any())).thenReturn(false);
        when(repository.save(any(ShortUrl.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ShortUrlResponse response = service.create(request);

        assertNotNull(response);
        assertEquals("https://spring.io", response.getOriginalUrl());
        assertNotNull(response.getShortCode());
        assertEquals(8, response.getShortCode().length());
        assertEquals(0L, response.getClickCount());
        assertTrue(response.isActive());

        verify(repository).save(any(ShortUrl.class));
    }

    @Test
    void shouldRejectInvalidUrl() {

        CreateShortUrlRequest request = new CreateShortUrlRequest();
        request.setOriginalUrl("not-a-url");

        assertThrows(
                InvalidUrlException.class,
                () -> service.create(request)
        );

        verify(repository, never()).save(any());
    }

    @Test
    void shouldGetUrlDetailsWithoutIncrementingClicks() {

        ShortUrl shortUrl = createShortUrl();

        when(repository.findByShortCode("ABC123"))
                .thenReturn(Optional.of(shortUrl));

        ShortUrlResponse response = service.getDetails("ABC123");

        assertEquals("https://spring.io", response.getOriginalUrl());
        assertEquals(0L, response.getClickCount());

        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUrlDoesNotExist() {

        when(repository.findByShortCode("INVALID"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.getDetails("INVALID")
        );
    }

    @Test
    void shouldUpdateUrl() {

        ShortUrl shortUrl = createShortUrl();

        when(repository.findByShortCode("ABC123"))
                .thenReturn(Optional.of(shortUrl));

        when(repository.save(any(ShortUrl.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateShortUrlRequest request = new UpdateShortUrlRequest();
        request.setOriginalUrl("https://openai.com");

        ShortUrlResponse response =
                service.update("ABC123", request);

        assertEquals(
                "https://openai.com",
                response.getOriginalUrl()
        );

        verify(repository).save(shortUrl);
    }

    @Test
    void shouldDeleteUrl() {

        ShortUrl shortUrl = createShortUrl();

        when(repository.findByShortCode("ABC123"))
                .thenReturn(Optional.of(shortUrl));

        when(repository.save(any(ShortUrl.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.delete("ABC123");

        assertFalse(shortUrl.isActive());
        assertEquals(UrlStatus.DELETED, shortUrl.getStatus());

        verify(repository).save(shortUrl);
    }

    @Test
    void shouldRedirectAndIncrementClickCount() {

        ShortUrl shortUrl = createShortUrl();

        when(repository.findByShortCode("ABC123"))
                .thenReturn(Optional.of(shortUrl));

        when(repository.save(any(ShortUrl.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String originalUrl = service.redirect("ABC123");

        assertEquals("https://spring.io", originalUrl);
        assertEquals(1L, shortUrl.getClickCount());
        assertNotNull(shortUrl.getLastAccessedAt());

        verify(repository).save(shortUrl);
    }

    @Test
    void shouldRejectExpiredUrl() {

        ShortUrl shortUrl = createShortUrl();

        shortUrl.setExpiresAt(
                LocalDateTime.now().minusMinutes(1)
        );

        when(repository.findByShortCode("ABC123"))
                .thenReturn(Optional.of(shortUrl));

        assertThrows(
                InvalidUrlException.class,
                () -> service.redirect("ABC123")
        );

        assertEquals(
                UrlStatus.EXPIRED,
                shortUrl.getStatus()
        );
    }

    private ShortUrl createShortUrl() {

        return ShortUrl.builder()
                .id("1")
                .originalUrl("https://spring.io")
                .shortCode("ABC123")
                .status(UrlStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .clickCount(0L)
                .active(true)
                .build();
    }
}