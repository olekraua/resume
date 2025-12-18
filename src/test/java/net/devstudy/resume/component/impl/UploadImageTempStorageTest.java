package net.devstudy.resume.component.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import net.devstudy.resume.annotation.EnableUploadImageTempStorage;
import net.devstudy.resume.model.UploadTempPath;

class UploadImageTempStorageTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @Test
    void createsAndDeletesTempFilesAroundAnnotatedMethod() {
        contextRunner.run(context -> {
            UploadImageTempStorage storage = context.getBean(UploadImageTempStorage.class);
            Probe probe = context.getBean(Probe.class);

            ProbeResult result = probe.captureTempPaths();

            assertTrue(result.existedInsideMethod());
            assertTrue(result.threadLocalWasSetInsideMethod());
            assertFalse(Files.exists(result.largeImagePath()));
            assertFalse(Files.exists(result.smallImagePath()));
            assertNull(storage.getCurrentUploadTempPath());
        });
    }

    @Test
    void alwaysCleansUpWhenTargetThrows() {
        contextRunner.run(context -> {
            UploadImageTempStorage storage = context.getBean(UploadImageTempStorage.class);
            Probe probe = context.getBean(Probe.class);

            RuntimeException ex = assertThrows(RuntimeException.class, probe::alwaysFails);
            assertEquals("boom", ex.getMessage());

            UploadTempPath lastPath = probe.getLastTempPath();
            assertNotNull(lastPath);
            assertFalse(Files.exists(lastPath.getLargeImagePath()));
            assertFalse(Files.exists(lastPath.getSmallImagePath()));
            assertNull(storage.getCurrentUploadTempPath());
        });
    }

    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    static class TestConfig {

        @Bean
        UploadImageTempStorage uploadImageTempStorage() {
            return new UploadImageTempStorage();
        }

        @Bean
        Probe probe(UploadImageTempStorage uploadImageTempStorage) {
            return new Probe(uploadImageTempStorage);
        }
    }

    static class Probe {
        private final UploadImageTempStorage uploadImageTempStorage;
        private final AtomicReference<UploadTempPath> lastTempPath = new AtomicReference<>();

        Probe(UploadImageTempStorage uploadImageTempStorage) {
            this.uploadImageTempStorage = uploadImageTempStorage;
        }

        @EnableUploadImageTempStorage
        ProbeResult captureTempPaths() {
            UploadTempPath tempPath = uploadImageTempStorage.getCurrentUploadTempPath();
            if (tempPath == null) {
                return new ProbeResult(null, null, false, false);
            }
            boolean existsInside = Files.exists(tempPath.getLargeImagePath()) && Files.exists(tempPath.getSmallImagePath());
            return new ProbeResult(tempPath.getLargeImagePath(), tempPath.getSmallImagePath(), existsInside, true);
        }

        @EnableUploadImageTempStorage
        void alwaysFails() {
            lastTempPath.set(uploadImageTempStorage.getCurrentUploadTempPath());
            throw new RuntimeException("boom");
        }

        UploadTempPath getLastTempPath() {
            return lastTempPath.get();
        }
    }

    record ProbeResult(Path largeImagePath, Path smallImagePath, boolean existedInsideMethod,
                       boolean threadLocalWasSetInsideMethod) {
    }
}
