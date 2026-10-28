package com.example.agentic.controller;

import com.example.agentic.service.UrlShortenerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final UrlShortenerService urlShortenerService;

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode) {

        String originalUrl =
                urlShortenerService.redirect(shortCode);

        return ResponseEntity
                .status(302)
                .header(HttpHeaders.LOCATION, originalUrl)
                .build();
    }

}