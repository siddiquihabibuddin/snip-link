package com.sniplink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

class ShortCodeGeneratorTest {

    private final ShortCodeGenerator generator = new ShortCodeGenerator();

    @Test
    void generatesCodeOfConfiguredLength() {
        assertThat(generator.generate()).hasSize(6);
        assertThat(new ShortCodeGenerator(10).generate()).hasSize(10);
    }

    @Test
    void generatesOnlyBase62Characters() {
        IntStream.range(0, 500).forEach(i ->
                assertThat(generator.generate()).matches("[A-Za-z0-9]+"));
    }

    @Test
    void generatesDistinctCodesInPractice() {
        Set<String> seen = new HashSet<>();
        IntStream.range(0, 1000).forEach(i -> seen.add(generator.generate()));
        // 6 chars of base62 -> collisions over 1000 draws are astronomically unlikely
        assertThat(seen).hasSizeGreaterThan(995);
    }

    @Test
    void generateUniqueSkipsTakenCodes() {
        Set<String> taken = new HashSet<>(Set.of("aaaaaa", "bbbbbb"));
        String code = generator.generateUnique(taken::contains);
        assertThat(taken).doesNotContain(code);
    }

    @Test
    void generateUniqueGivesUpWhenEverythingIsTaken() {
        assertThatThrownBy(() -> generator.generateUnique(any -> true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("could not generate");
    }

    @Test
    void rejectsNonPositiveLength() {
        assertThatThrownBy(() -> new ShortCodeGenerator(0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
