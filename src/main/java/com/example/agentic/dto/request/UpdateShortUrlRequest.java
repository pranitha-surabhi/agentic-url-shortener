package com.example.agentic.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateShortUrlRequest {

    @NotBlank(message = "Original URL cannot be empty")
    private String originalUrl;
}