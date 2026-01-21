package net.devstudy.resume.media.component;

import java.nio.file.Path;

import org.springframework.lang.NonNull;

public interface ImageOptimizator {

    void optimize(@NonNull Path image);
}
