package net.devstudy.resume.media.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadCertificateResult {
    private String certificateName;
    private String largeUrl;
    private String smallUrl;
}
