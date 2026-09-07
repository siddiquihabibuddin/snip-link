package com.sniplink;

import java.time.Instant;

/** A single shortened link. */
public record Link(String code, String url, Instant createdAt) {
}
