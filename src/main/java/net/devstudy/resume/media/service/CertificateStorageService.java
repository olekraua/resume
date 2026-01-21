package net.devstudy.resume.media.service;

import org.springframework.web.multipart.MultipartFile;

import net.devstudy.resume.media.model.UploadCertificateResult;

public interface CertificateStorageService {
    UploadCertificateResult store(MultipartFile file);
}
