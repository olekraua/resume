package net.devstudy.resume.component;

import java.io.IOException;
import java.nio.file.Path;

import jakarta.annotation.Nonnull;

/**
 * Image resizer interface
 * Compatible with Java 21 + Spring Boot 3
 */
public interface ImageResizer {

    void resize(@Nonnull Path sourceImageFile,
                @Nonnull Path destImageFile,
                int width,
                int height) throws IOException;
}

