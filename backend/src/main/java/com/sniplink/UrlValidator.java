package com.sniplink;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.stereotype.Component;

/**
 * Validates URLs submitted for shortening. Only absolute http(s) URLs with a host
 * are accepted - everything else (blank, relative, ftp:, javascript:, malformed) is
 * rejected. Kept deliberately small so it is easy to unit test.
 */
@Component
public class UrlValidator {

    private static final int MAX_LENGTH = 2048;

    public boolean isValid(String url) {
        if (url == null) {
            return false;
        }
        String trimmed = url.trim();
        if (trimmed.isEmpty() || trimmed.length() > MAX_LENGTH) {
            return false;
        }
        try {
            URI uri = new URI(trimmed);
            String scheme = uri.getScheme();
            if (scheme == null) {
                return false;
            }
            scheme = scheme.toLowerCase();
            if (!scheme.equals("http") && !scheme.equals("https")) {
                return false;
            }
            return uri.getHost() != null && !uri.getHost().isBlank();
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
