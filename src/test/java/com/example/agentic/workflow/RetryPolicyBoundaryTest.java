package com.example.agentic.workflow;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RetryPolicyBoundaryTest {
    @Test void failClosedAtConfiguredRetryLimit() {
        RetryPolicy policy = new RetryPolicy(3);
        assertFalse(policy.exhausted(1));
        assertFalse(policy.exhausted(2));
        assertTrue(policy.exhausted(3));
        assertTrue(policy.exhausted(4));
        assertThrows(IllegalArgumentException.class, () -> new RetryPolicy(0));
    }
}
