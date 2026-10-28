package com.example.agentic.workflow;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Fail-closed shared-secret approval guard for a local prototype. */
@Component
public class ReviewerAuthorization {
    @Value("${agentic.reviewer.token:}")
    private String configuredToken = "";

    public void verify(String presentedToken) {
        if (configuredToken == null || configuredToken.length() < 32) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Human approval requires configured reviewer identity provider");
        }
        if (presentedToken == null || !MessageDigest.isEqual(
                configuredToken.getBytes(StandardCharsets.UTF_8),
                presentedToken.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Reviewer authorization failed");
        }
    }
}
