package net.devstudy.resume.component;

import java.nio.file.Path;

import jakarta.annotation.Nonnull;

/**
 * Image optimizer interface
 * Compatible with Java 21 + Spring Boot 3
 */
public interface ImageOptimizator {

    void optimize(@Nonnull Path image);
}

