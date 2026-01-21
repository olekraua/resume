package net.devstudy.resume.media.model;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class UploadTempPathTest {

    @Test
    void createsTempFiles() throws IOException {
        UploadTempPath tempPath = new UploadTempPath();

        Path large = tempPath.getLargeImagePath();
        Path small = tempPath.getSmallImagePath();

        assertNotNull(large);
        assertNotNull(small);
        assertNotEquals(large, small);
        assertTrue(Files.exists(large));
        assertTrue(Files.exists(small));

        Files.deleteIfExists(large);
        Files.deleteIfExists(small);
    }
}
