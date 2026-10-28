package com.example.agentic.controller;

import com.example.agentic.used.constants.ApiConstants;
import com.example.agentic.used.response.ApiResponse;
import com.example.agentic.dto.request.CreateShortUrlRequest;
import com.example.agentic.dto.request.UpdateShortUrlRequest;
import com.example.agentic.dto.response.ShortUrlResponse;
import com.example.agentic.service.UrlShortenerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/urls")
@RequiredArgsConstructor
public class UrlController {

    private final UrlShortenerService urlShortenerService;

    @PostMapping
    public ResponseEntity<ApiResponse<ShortUrlResponse>> create(
            @Valid @RequestBody CreateShortUrlRequest request) {

        ShortUrlResponse response = urlShortenerService.create(request);

        return ResponseEntity.ok(
                ApiResponse.<ShortUrlResponse>builder()
                        .success(true)
                        .message(ApiConstants.URL_CREATED)
                        .data(response)
                        .build());
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<ApiResponse<ShortUrlResponse>> getDetails(
            @PathVariable String shortCode) {

        ShortUrlResponse response =
                urlShortenerService.getDetails(shortCode);

        return ResponseEntity.ok(
                ApiResponse.<ShortUrlResponse>builder()
                        .success(true)
                        .message(ApiConstants.SUCCESS)
                        .data(response)
                        .build());
    }

    @PutMapping("/{shortCode}")
    public ResponseEntity<ApiResponse<ShortUrlResponse>> update(
            @PathVariable String shortCode,
            @Valid @RequestBody UpdateShortUrlRequest request) {

        ShortUrlResponse response =
                urlShortenerService.update(shortCode, request);

        return ResponseEntity.ok(
                ApiResponse.<ShortUrlResponse>builder()
                        .success(true)
                        .message(ApiConstants.URL_UPDATED)
                        .data(response)
                        .build());
    }

    @DeleteMapping("/{shortCode}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String shortCode) {

        urlShortenerService.delete(shortCode);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message(ApiConstants.URL_DELETED)
                        .build());
    }

}