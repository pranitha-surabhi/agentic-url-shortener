package com.example.agentic.used.validator;

import java.net.URI;

public final class UrlValidator {

    private UrlValidator() {
    }

    public static boolean isValid(String url) {

        try {

            URI uri = new URI(url);

            return ("http".equalsIgnoreCase(uri.getScheme())
                    || "https".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null
                    && !uri.getHost().isBlank()
                    && uri.getUserInfo() == null;

        } catch (Exception ex) {

            return false;
        }
    }
}