package net.devstudy.resume.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import net.devstudy.resume.model.UploadCertificateResult;
import net.devstudy.resume.service.CertificateStorageService;

@Service
@RequiredArgsConstructor
public class CertificateStorageServiceImpl implements CertificateStorageService {

    @Value("${upload.certificates.dir:uploads/certificates}")
    private String certificatesDir;

    @Override
    public UploadCertificateResult store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Empty certificate file");
        }
        try {
            Path dir = Path.of(certificatesDir);
            Files.createDirectories(dir);
            String ext = getExtension(file.getOriginalFilename());
            String baseName = UUID.randomUUID().toString();
            String fileName = baseName + (ext.isEmpty() ? "" : "." + ext);
            Path target = dir.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String url = "/uploads/certificates/" + fileName;
            String certName = stripExtension(file.getOriginalFilename());
            return new UploadCertificateResult(certName, url, url);
        } catch (IOException e) {
            throw new RuntimeException("Can't store certificate file", e);
        }
    }

    private String getExtension(String name) {
        if (name == null) return "";
        int idx = name.lastIndexOf('.');
        return (idx >= 0 && idx < name.length() - 1) ? name.substring(idx + 1) : "";
    }

    private String stripExtension(String name) {
        if (name == null) return "";
        int idx = name.lastIndexOf('.');
        return (idx > 0) ? name.substring(0, idx) : name;
    }
}
