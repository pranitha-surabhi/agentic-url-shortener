package com.example.agentic.service;

import com.example.agentic.used.constants.ApiConstants;
import com.example.agentic.used.enums.UrlStatus;
import com.example.agentic.used.validator.UrlValidator;
import com.example.agentic.dto.request.CreateShortUrlRequest;
import com.example.agentic.dto.request.UpdateShortUrlRequest;
import com.example.agentic.dto.response.ShortUrlResponse;
import com.example.agentic.exceptions.InvalidUrlException;
import com.example.agentic.exceptions.ResourceNotFoundException;
import com.example.agentic.models.ShortUrl;
import com.example.agentic.repository.ShortUrlRepository;
import com.example.agentic.util.ShortCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UrlShortenerService {

    private final ShortUrlRepository repository;

    @Value("${app.public-base-url:http://localhost:9091}")
    private String publicBaseUrl = "http://localhost:9091";

    public ShortUrlResponse create(CreateShortUrlRequest request) {

        if (!UrlValidator.isValid(request.getOriginalUrl())) {
            throw new InvalidUrlException(ApiConstants.INVALID_URL);
        }

        ShortUrl shortUrl = ShortUrl.builder()
                .id(UUID.randomUUID().toString())
                .originalUrl(request.getOriginalUrl())
                .shortCode(generateUniqueShortCode())
                .status(UrlStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .expiresAt(
                        request.getExpiryInHours() == null
                                ? null
                                : LocalDateTime.now().plusHours(request.getExpiryInHours())
                )
                .clickCount(0L)
                .active(true)
                .build();

        repository.save(shortUrl);

        return map(shortUrl);
    }

    public ShortUrlResponse getDetails(String shortCode) {

        ShortUrl shortUrl = repository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException(ApiConstants.URL_NOT_FOUND));

        validate(shortUrl);

        return map(shortUrl);
    }

    public ShortUrlResponse update(
            String shortCode,
            UpdateShortUrlRequest request) {

        ShortUrl shortUrl = repository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException(ApiConstants.URL_NOT_FOUND));

        if (!UrlValidator.isValid(request.getOriginalUrl())) {
            throw new InvalidUrlException(ApiConstants.INVALID_URL);
        }
        shortUrl.setOriginalUrl(request.getOriginalUrl());
        shortUrl.setUpdatedAt(LocalDateTime.now());

        repository.save(shortUrl);

        return map(shortUrl);
    }

    public void delete(String shortCode) {

        ShortUrl shortUrl = repository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException(ApiConstants.URL_NOT_FOUND));

        shortUrl.setStatus(UrlStatus.DELETED);
        shortUrl.setActive(false);

        repository.save(shortUrl);
    }

    private void validate(ShortUrl shortUrl) {

        if (!shortUrl.isActive()) {
            throw new InvalidUrlException("URL is inactive");
        }

        if (shortUrl.getExpiresAt() != null &&
                LocalDateTime.now().isAfter(shortUrl.getExpiresAt())) {

            shortUrl.setStatus(UrlStatus.EXPIRED);

            throw new InvalidUrlException("URL has expired");
        }
    }

    private String generateUniqueShortCode() {

        String shortCode;

        do {

            shortCode = ShortCodeGenerator.generate();

        } while (repository.existsByShortCode(shortCode));

        return shortCode;
    }

    private ShortUrlResponse map(ShortUrl shortUrl) {

        return ShortUrlResponse.builder()
                .id(shortUrl.getId())
                .originalUrl(shortUrl.getOriginalUrl())
                .shortCode(shortUrl.getShortCode())
                .shortUrl(publicBaseUrl.replaceAll("/+$", "") + "/" + shortUrl.getShortCode())
                .createdAt(shortUrl.getCreatedAt())
                .updatedAt(shortUrl.getUpdatedAt())
                .expiresAt(shortUrl.getExpiresAt())
                .clickCount(shortUrl.getClickCount())
                .active(shortUrl.isActive())
                .build();
    }

    public synchronized String redirect(String shortCode) {

        ShortUrl shortUrl = repository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException(ApiConstants.URL_NOT_FOUND));

        validate(shortUrl);

        shortUrl.setClickCount(shortUrl.getClickCount() + 1);

        shortUrl.setLastAccessedAt(LocalDateTime.now());

        repository.save(shortUrl);

        return shortUrl.getOriginalUrl();
    }

}