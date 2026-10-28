package com.example.agentic.util;

import java.util.UUID;

public final class ShortCodeGenerator {

    private ShortCodeGenerator() {
    }

    public static String generate() {

        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
    }
}