package com.example.agentic.controller;

import com.example.agentic.service.UrlShortenerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RedirectController.class)
class RedirectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlShortenerService urlShortenerService;

    @Test
    void shouldRedirectToOriginalUrl() throws Exception {

        when(urlShortenerService.redirect("ABC12345"))
                .thenReturn("https://spring.io");

        mockMvc.perform(
                        get("/ABC12345")
                )
                .andExpect(status().isFound())
                .andExpect(
                        header().string(
                                HttpHeaders.LOCATION,
                                "https://spring.io"
                        )
                );
    }
}