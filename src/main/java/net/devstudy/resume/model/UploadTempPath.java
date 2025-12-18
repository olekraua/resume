package net.devstudy.resume.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class UploadTempPath extends AbstractModel {

    private final Path largeImagePath;
    private final Path smallImagePath;

    public UploadTempPath() throws IOException {
        largeImagePath = Files.createTempFile("large", ".jpg");
        smallImagePath = Files.createTempFile("small", ".jpg");
    }

    public Path getLargeImagePath() {
        return largeImagePath;
    }

    public Path getSmallImagePath() {
        return smallImagePath;
    }
}

