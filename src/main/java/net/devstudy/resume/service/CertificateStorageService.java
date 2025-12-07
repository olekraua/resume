package net.devstudy.resume.service;

import org.springframework.web.multipart.MultipartFile;

import net.devstudy.resume.model.UploadCertificateResult;

public interface CertificateStorageService {
    UploadCertificateResult store(MultipartFile file);
}
