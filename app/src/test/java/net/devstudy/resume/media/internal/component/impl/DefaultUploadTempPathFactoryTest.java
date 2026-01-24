package net.devstudy.resume.media.internal.component.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import net.devstudy.resume.media.internal.model.UploadTempPath;

class DefaultUploadTempPathFactoryTest {

    private final DefaultUploadTempPathFactory factory = new DefaultUploadTempPathFactory();

    @Test
    void createProducesTwoTempFiles() throws IOException {
        UploadTempPath tempPath = factory.create();

        assertNotNull(tempPath);
        Path large = tempPath.getLargeImagePath();
        Path small = tempPath.getSmallImagePath();
        assertNotNull(large);
        assertNotNull(small);
        assertTrue(Files.exists(large));
        assertTrue(Files.exists(small));
        assertTrue(large.toString().endsWith(".jpg"));
        assertTrue(small.toString().endsWith(".jpg"));
    }
}
