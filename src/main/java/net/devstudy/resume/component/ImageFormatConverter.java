package net.devstudy.resume.component;

import java.io.IOException;
import java.nio.file.Path;

import jakarta.annotation.Nonnull;

/**
 * Image format converter
 * Works with Java 21 + Spring Boot 3
 */
public interface ImageFormatConverter {

    void convert(@Nonnull Path sourceImageFile, @Nonnull Path destImageFile) throws IOException;
}
