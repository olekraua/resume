package net.devstudy.resume.component;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Template resolver interface
 * Compatible with Java 21 + Spring Boot 3
 */
public interface TemplateResolver {

    @Nonnull
    String resolve(@Nonnull String template, @Nullable Object model);
}
