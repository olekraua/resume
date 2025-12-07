package net.devstudy.resume.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.devstudy.resume.service.PhotoStorageService;

@Service
public class PhotoStorageServiceImpl implements PhotoStorageService {

    @Value("${upload.photos.dir:uploads/photos}")
    private String photosDir;

    @Override
    public String[] store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Empty photo file");
        }
        try {
            Path dir = Path.of(photosDir);
            Files.createDirectories(dir);
            String ext = getExtension(file.getOriginalFilename());
            String baseName = UUID.randomUUID().toString();
            String fileName = baseName + (ext.isEmpty() ? "" : "." + ext);
            String smallName = baseName + "-sm" + (ext.isEmpty() ? "" : "." + ext);

            Path largeTarget = dir.resolve(fileName);
            Path smallTarget = dir.resolve(smallName);

            Files.copy(file.getInputStream(), largeTarget, StandardCopyOption.REPLACE_EXISTING);
            // simple small copy (без ресайзу; можна додати ресайз пізніше)
            Files.copy(file.getInputStream(), smallTarget, StandardCopyOption.REPLACE_EXISTING);

            String baseUrl = "/uploads/photos/";
            return new String[] { baseUrl + fileName, baseUrl + smallName };
        } catch (IOException e) {
            throw new RuntimeException("Can't store photo file", e);
        }
    }

    private String getExtension(String name) {
        if (name == null) return "";
        int idx = name.lastIndexOf('.');
        return (idx >= 0 && idx < name.length() - 1) ? name.substring(idx + 1) : "";
    }
}
