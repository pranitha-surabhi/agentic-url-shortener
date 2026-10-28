package com.example.agentic.controller;

import com.example.agentic.dto.request.CreateShortUrlRequest;
import com.example.agentic.dto.response.ShortUrlResponse;
import com.example.agentic.service.UrlShortenerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UrlController.class)
class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlShortenerService urlShortenerService;

    @Test
    void shouldCreateShortUrl() throws Exception {

        ShortUrlResponse response = ShortUrlResponse.builder()
                .id("1")
                .originalUrl("https://spring.io")
                .shortCode("ABC12345")
                .shortUrl("http://localhost:8080/ABC12345")
                .createdAt(LocalDateTime.now())
                .clickCount(0L)
                .active(true)
                .build();

        when(urlShortenerService.create(any(CreateShortUrlRequest.class)))
                .thenReturn(response);

        String requestBody = """
                {
                    "originalUrl": "https://spring.io",
                    "expiryInHours": 24
                }
                """;

        mockMvc.perform(
                        post("/api/v1/urls")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.shortCode").value("ABC12345"))
                .andExpect(jsonPath("$.data.originalUrl")
                        .value("https://spring.io"));
    }

    @Test
    void shouldRejectEmptyUrl() throws Exception {

        String requestBody = """
                {
                    "originalUrl": "",
                    "expiryInHours": 24
                }
                """;

        mockMvc.perform(
                        post("/api/v1/urls")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }
}