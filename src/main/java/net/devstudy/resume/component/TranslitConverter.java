package net.devstudy.resume.component;

import jakarta.annotation.Nonnull;

/**
 * Translit converter interface
 * Compatible with Java 21 + Spring Boot 3
 */
public interface TranslitConverter {

    @Nonnull
    String translit(@Nonnull String text);
}

