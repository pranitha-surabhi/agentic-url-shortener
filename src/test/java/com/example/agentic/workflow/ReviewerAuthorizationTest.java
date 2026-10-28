package com.example.agentic.workflow;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

class ReviewerAuthorizationTest {
    @Test
    void failsClosedUnlessAReviewerSecretIsConfigured() {
        ReviewerAuthorization guard = new ReviewerAuthorization();
        assertThrows(ResponseStatusException.class, () -> guard.verify("wrong"));
        ReflectionTestUtils.setField(guard, "configuredToken",
                "a-long-reviewer-token-of-at-least-32-characters");
        guard.verify("a-long-reviewer-token-of-at-least-32-characters");
        var denied = assertThrows(ResponseStatusException.class,
                () -> guard.verify("incorrect"));
        assertEquals(HttpStatus.FORBIDDEN.value(), denied.getStatusCode().value());
    }
}
