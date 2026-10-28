package com.example.agentic.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateShortUrlRequest {

    @NotBlank(message = "Original URL cannot be empty")
    private String originalUrl;

    /**
     * Optional.
     * If null, URL never expires.
     */
    private Long expiryInHours;
}