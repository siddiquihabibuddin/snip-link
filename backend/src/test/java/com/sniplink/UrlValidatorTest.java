package com.sniplink;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class UrlValidatorTest {

    private final UrlValidator validator = new UrlValidator();

    @ParameterizedTest
    @ValueSource(strings = {
            "http://example.com",
            "https://example.com",
            "https://example.com/path?q=1#frag",
            "https://sub.domain.example.co.uk/a/b/c",
            "  https://example.com/trimmed  "
    })
    void acceptsAbsoluteHttpUrls(String url) {
        assertThat(validator.isValid(url)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "   ",
            "example.com",
            "//example.com",
            "/relative/path",
            "ftp://example.com",
            "javascript:alert(1)",
            "mailto:a@b.com",
            "http://",
            "https:// space in host",
            "ht!tp://bad"
    })
    void rejectsEverythingElse(String url) {
        assertThat(validator.isValid(url)).isFalse();
    }

    @Test
    void rejectsOverlyLongUrls() {
        String longUrl = "https://example.com/" + "x".repeat(3000);
        assertThat(validator.isValid(longUrl)).isFalse();
    }
}
