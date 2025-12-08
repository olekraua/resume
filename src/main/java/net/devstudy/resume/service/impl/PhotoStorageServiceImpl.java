package net.devstudy.resume.service.impl;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.devstudy.resume.service.PhotoStorageService;

@Service
public class PhotoStorageServiceImpl implements PhotoStorageService {

    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024; // 5MB
    private static final int MIN_DIMENSION = 400;

    @Value("${upload.photos.dir:uploads/photos}")
    private String photosDir;

    @Override
    public String[] store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Empty photo file");
        }
        try {
            byte[] data = file.getBytes();
            validatePhoto(file, data);

            Path dir = Path.of(photosDir);
            Files.createDirectories(dir);
            String ext = getExtension(file.getOriginalFilename());
            String baseName = UUID.randomUUID().toString();
            String fileName = baseName + (ext.isEmpty() ? "" : "." + ext);
            String smallName = baseName + "-sm" + (ext.isEmpty() ? "" : "." + ext);

            Path largeTarget = dir.resolve(fileName);
            Path smallTarget = dir.resolve(smallName);

            Files.write(largeTarget, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            // simple small copy (без ресайзу; можна додати ресайз пізніше)
            Files.write(smallTarget, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            String baseUrl = "/uploads/photos/";
            return new String[] { baseUrl + fileName, baseUrl + smallName };
        } catch (IOException e) {
            throw new RuntimeException("Can't store photo file", e);
        }
    }

    private void validatePhoto(MultipartFile file, byte[] data) {
        if (data.length > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("Фото завелике: максимум 5MB");
        }
        String ext = getExtension(file.getOriginalFilename()).toLowerCase();
        if (!(ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png"))) {
            throw new IllegalArgumentException("Дозволені лише jpg або png");
        }
        try {
            var img = javax.imageio.ImageIO.read(new ByteArrayInputStream(data));
            if (img == null) {
                throw new IllegalArgumentException("Неправильний формат зображення");
            }
            if (img.getWidth() < MIN_DIMENSION || img.getHeight() < MIN_DIMENSION) {
                throw new IllegalArgumentException("Мінімальний розмір фото 400x400");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Неможливо прочитати зображення", e);
        }
    }

    private String getExtension(String name) {
        if (name == null) return "";
        int idx = name.lastIndexOf('.');
        return (idx >= 0 && idx < name.length() - 1) ? name.substring(idx + 1) : "";
    }
}
