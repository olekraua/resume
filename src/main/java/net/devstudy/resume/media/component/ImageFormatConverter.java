package net.devstudy.resume.media.component;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.lang.NonNull;

public interface ImageFormatConverter {

    void convert(@NonNull Path sourceImageFile, @NonNull Path destImageFile) throws IOException;
}
