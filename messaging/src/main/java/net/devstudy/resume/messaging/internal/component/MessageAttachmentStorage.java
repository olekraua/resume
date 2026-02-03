package net.devstudy.resume.messaging.internal.component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import net.devstudy.resume.messaging.internal.config.MessageAttachmentProperties;

@Component
public class MessageAttachmentStorage {

    private final MessageAttachmentProperties properties;

    public MessageAttachmentStorage(MessageAttachmentProperties properties) {
        this.properties = properties;
    }

    public StoredAttachment store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Attachment is empty");
        }
        String contentType = normalizeContentType(file.getContentType());
        String extension = resolveExtension(contentType);
        String storageKey = generateStorageKey(extension);
        Path baseDir = resolveBaseDir();
        Path target = baseDir.resolve(storageKey).normalize();
        if (!target.startsWith(baseDir)) {
            throw new IllegalStateException("Invalid attachment path");
        }
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store attachment", ex);
        }
        String originalName = normalizeOriginalName(file.getOriginalFilename());
        return new StoredAttachment(storageKey, originalName, contentType, file.getSize());
    }

    public Path resolvePath(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return null;
        }
        if (storageKey.contains("/") || storageKey.contains("\\")) {
            return null;
        }
        Path baseDir = resolveBaseDir();
        Path target = baseDir.resolve(storageKey).normalize();
        if (!target.startsWith(baseDir)) {
            return null;
        }
        return target;
    }

    private Path resolveBaseDir() {
        Path baseDir = Path.of(properties.getDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(baseDir);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to create attachment directory", ex);
        }
        return baseDir;
    }

    private String generateStorageKey(String extension) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        if (extension == null || extension.isBlank()) {
            return uuid;
        }
        return uuid + extension;
    }

    private String resolveExtension(String contentType) {
        if (contentType == null) {
            return "";
        }
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "application/pdf" -> ".pdf";
            default -> "";
        };
    }

    private String normalizeOriginalName(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return "attachment";
        }
        String trimmed = originalName.trim();
        if (trimmed.length() > 255) {
            return trimmed.substring(trimmed.length() - 255);
        }
        return trimmed;
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null) {
            return "application/octet-stream";
        }
        return contentType.trim().toLowerCase(Locale.ROOT);
    }

    public record StoredAttachment(String storageKey, String originalName, String contentType, long size) {
    }
}
