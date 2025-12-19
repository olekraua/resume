package net.devstudy.resume.component.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import net.devstudy.resume.annotation.EnableUploadImageTempStorage;
import net.devstudy.resume.component.UploadTempPathFactory;
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

            assertTrue(probe.existedInsideBeforeThrow());
            UploadTempPath lastPath = probe.getLastTempPath();
            assertNotNull(lastPath);
            assertFalse(Files.exists(lastPath.getLargeImagePath()));
            assertFalse(Files.exists(lastPath.getSmallImagePath()));
            assertNull(storage.getCurrentUploadTempPath());
        });
    }

    @Test
    void deleteQuietlyHandlesNullMissingAndExistingPaths() {
        UploadTempPathFactory factory = () -> {
            throw new IOException("unused");
        };
        UploadImageTempStorage storage = new UploadImageTempStorage(factory);

        assertDoesNotThrow(() -> storage.deleteQuietly(null));

        Path missingPath = Path.of("target", "missing-file-" + System.nanoTime());
        assertDoesNotThrow(() -> storage.deleteQuietly(missingPath));

        assertDoesNotThrow(() -> {
            Path tempFile = Files.createTempFile("resume-test-", ".tmp");
            assertTrue(Files.exists(tempFile));
            storage.deleteQuietly(tempFile);
            assertFalse(Files.exists(tempFile));
        });
    }

    @Test
    void threadLocalIsIsolatedAcrossThreads() {
        contextRunner.run(context -> {
            Probe probe = context.getBean(Probe.class);
            CountDownLatch entered = new CountDownLatch(2);
            CountDownLatch release = new CountDownLatch(1);
            ExecutorService executor = Executors.newFixedThreadPool(2);
            try {
                Future<UploadTempPath> first = executor.submit(
                        () -> probe.captureTempPathInParallel(entered, release));
                Future<UploadTempPath> second = executor.submit(
                        () -> probe.captureTempPathInParallel(entered, release));

                assertTrue(entered.await(5, TimeUnit.SECONDS));
                release.countDown();

                UploadTempPath firstPath = first.get(5, TimeUnit.SECONDS);
                UploadTempPath secondPath = second.get(5, TimeUnit.SECONDS);

                assertNotNull(firstPath);
                assertNotNull(secondPath);
                assertNotEquals(firstPath.getLargeImagePath(), secondPath.getLargeImagePath());
                assertNotEquals(firstPath.getSmallImagePath(), secondPath.getSmallImagePath());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                release.countDown();
                executor.shutdownNow();
            }
        });
    }

    @Test
    void adviceThrowsIllegalStateWhenFactoryFails() {
        ApplicationContextRunner failingContextRunner = new ApplicationContextRunner()
                .withUserConfiguration(FailingFactoryConfig.class);

        failingContextRunner.run(context -> {
            FailingProbe probe = context.getBean(FailingProbe.class);

            IllegalStateException ex = assertThrows(IllegalStateException.class, probe::invoke);
            assertTrue(ex.getMessage().startsWith("Can't create temp image files:"));
        });
    }

    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    static class TestConfig {

        @Bean
        UploadImageTempStorage uploadImageTempStorage(UploadTempPathFactory uploadTempPathFactory) {
            return new UploadImageTempStorage(uploadTempPathFactory);
        }

        @Bean
        UploadTempPathFactory uploadTempPathFactory() {
            return UploadTempPath::new;
        }

        @Bean
        Probe probe(UploadImageTempStorage uploadImageTempStorage) {
            return new Probe(uploadImageTempStorage);
        }
    }

    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    static class FailingFactoryConfig {

        @Bean
        UploadImageTempStorage uploadImageTempStorage(UploadTempPathFactory uploadTempPathFactory) {
            return new UploadImageTempStorage(uploadTempPathFactory);
        }

        @Bean
        UploadTempPathFactory uploadTempPathFactory() {
            return () -> {
                throw new IOException("disk is full");
            };
        }

        @Bean
        FailingProbe failingProbe() {
            return new FailingProbe();
        }
    }

    static class Probe {
        private final UploadImageTempStorage uploadImageTempStorage;
        private final AtomicReference<UploadTempPath> lastTempPath = new AtomicReference<>();
        private final AtomicBoolean existedInsideBeforeThrow = new AtomicBoolean(false);

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
            UploadTempPath tempPath = uploadImageTempStorage.getCurrentUploadTempPath();
            lastTempPath.set(tempPath);
            boolean existsInside = tempPath != null
                    && Files.exists(tempPath.getLargeImagePath())
                    && Files.exists(tempPath.getSmallImagePath());
            existedInsideBeforeThrow.set(existsInside);
            throw new RuntimeException("boom");
        }

        UploadTempPath getLastTempPath() {
            return lastTempPath.get();
        }

        boolean existedInsideBeforeThrow() {
            return existedInsideBeforeThrow.get();
        }

        @EnableUploadImageTempStorage
        UploadTempPath captureTempPathInParallel(CountDownLatch entered, CountDownLatch release) {
            UploadTempPath tempPath = uploadImageTempStorage.getCurrentUploadTempPath();
            entered.countDown();
            try {
                release.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            return tempPath;
        }
    }

    static class FailingProbe {

        @EnableUploadImageTempStorage
        void invoke() {
            // no-op
        }
    }

    record ProbeResult(Path largeImagePath, Path smallImagePath, boolean existedInsideMethod,
                       boolean threadLocalWasSetInsideMethod) {
    }
}
