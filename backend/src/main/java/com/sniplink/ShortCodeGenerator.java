package com.sniplink;

import java.security.SecureRandom;
import java.util.function.Predicate;

import org.springframework.stereotype.Component;

/**
 * Generates short, URL-safe base62 codes. On collision it retries with a fresh code
 * a bounded number of times before giving up, so the caller never loops forever.
 */
@Component
public class ShortCodeGenerator {

    private static final String ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int DEFAULT_LENGTH = 6;
    private static final int MAX_ATTEMPTS = 10;

    private final SecureRandom random = new SecureRandom();
    private final int length;

    public ShortCodeGenerator() {
        this(DEFAULT_LENGTH);
    }

    ShortCodeGenerator(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("length must be >= 1");
        }
        this.length = length;
    }

    /** Generate a single random code of the configured length. */
    public String generate() {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    /**
     * Generate a code that is not already taken, according to {@code isTaken}.
     *
     * @throws IllegalStateException if no free code is found within the attempt budget
     */
    public String generateUnique(Predicate<String> isTaken) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String candidate = generate();
            if (!isTaken.test(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException(
                "could not generate a free short code after " + MAX_ATTEMPTS + " attempts");
    }
}
