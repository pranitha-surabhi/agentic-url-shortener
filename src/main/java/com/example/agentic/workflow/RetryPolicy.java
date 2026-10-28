package com.example.agentic.workflow;

public record RetryPolicy(
        int maxAttempts
) {

    public boolean exhausted(int attempts) { return attempts >= maxAttempts; }

    public RetryPolicy {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException(
                    "maxAttempts must be at least 1"
            );
        }
    }
}