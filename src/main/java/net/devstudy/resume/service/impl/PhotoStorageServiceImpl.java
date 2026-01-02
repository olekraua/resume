package net.devstudy.resume.service.impl;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.devstudy.resume.component.ImageFormatConverter;
import net.devstudy.resume.component.ImageOptimizator;
import net.devstudy.resume.component.ImageResizer;
import net.devstudy.resume.config.PhotoUploadProperties;
import net.devstudy.resume.service.PhotoStorageService;

@Service
public class PhotoStorageServiceImpl implements PhotoStorageService {

    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024; // 5MB
    private static final int MIN_DIMENSION = 400;

    private final ImageOptimizator imageOptimizator;
    private final ImageFormatConverter pngToJpegImageFormatConverter;
    private final ImageResizer imageResizer;
    private final PhotoUploadProperties photoUploadProperties;

    public PhotoStorageServiceImpl(ImageOptimizator imageOptimizator,
            ImageFormatConverter pngToJpegImageFormatConverter,
            ImageResizer imageResizer,
            PhotoUploadProperties photoUploadProperties) {
        this.imageOptimizator = imageOptimizator;
        this.pngToJpegImageFormatConverter = pngToJpegImageFormatConverter;
        this.imageResizer = imageResizer;
        this.photoUploadProperties = photoUploadProperties;
    }

    @Override
    public String[] store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Empty photo file");
        }
        try {
            byte[] data = file.getBytes();
            validatePhoto(file, data);

            Path dir = Path.of(photoUploadProperties.getDir());
            Files.createDirectories(dir);
            String ext = getExtension(file.getOriginalFilename());
            boolean isPng = isPng(file, ext);
            String targetExt = isPng ? "jpg" : ext;
            String baseName = UUID.randomUUID().toString();
            String fileName = baseName + (targetExt.isEmpty() ? "" : "." + targetExt);
            String smallName = baseName + "-sm" + (targetExt.isEmpty() ? "" : "." + targetExt);

            Path largeTarget = dir.resolve(fileName);
            Path smallTarget = dir.resolve(smallName);

            if (isPng) {
                Path tmp = Files.createTempFile("resume-photo-", ".png");
                try {
                    Files.write(tmp, data, StandardOpenOption.TRUNCATE_EXISTING);
                    pngToJpegImageFormatConverter.convert(tmp, largeTarget);
                } finally {
                    Files.deleteIfExists(tmp);
                }
            } else {
                Files.write(largeTarget, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
            resizePhoto(largeTarget, smallTarget);
            imageOptimizator.optimize(largeTarget);
            imageOptimizator.optimize(smallTarget);

            String baseUrl = "/uploads/photos/";
            return new String[] { baseUrl + fileName, baseUrl + smallName };
        } catch (IOException e) {
            throw new RuntimeException("Can't store photo file", e);
        }
    }

    private void resizePhoto(Path largeTarget, Path smallTarget) throws IOException {
        imageResizer.resize(largeTarget, smallTarget,
                photoUploadProperties.getSmallWidth(),
                photoUploadProperties.getSmallHeight());
        imageResizer.resize(largeTarget, largeTarget,
                photoUploadProperties.getLargeWidth(),
                photoUploadProperties.getLargeHeight());
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

    private boolean isPng(MultipartFile file, String ext) {
        if ("png".equalsIgnoreCase(ext)) {
            return true;
        }
        String contentType = file.getContentType();
        return contentType != null && contentType.toLowerCase(Locale.ROOT).contains("png");
    }
}
