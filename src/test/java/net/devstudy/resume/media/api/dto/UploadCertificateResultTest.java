package net.devstudy.resume.media.api.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class UploadCertificateResultTest {

    @Test
    void defaultConstructorSetsNulls() {
        UploadCertificateResult result = new UploadCertificateResult();

        assertNull(result.getCertificateName());
        assertNull(result.getLargeUrl());
        assertNull(result.getSmallUrl());
    }

    @Test
    void allArgsConstructorSetsFields() {
        UploadCertificateResult result = new UploadCertificateResult("Cert", "large", "small");

        assertEquals("Cert", result.getCertificateName());
        assertEquals("large", result.getLargeUrl());
        assertEquals("small", result.getSmallUrl());
    }

    @Test
    void equalsAndHashCodeUseAllFields() {
        UploadCertificateResult first = new UploadCertificateResult("Cert", "large", "small");
        UploadCertificateResult second = new UploadCertificateResult("Cert", "large", "small");
        UploadCertificateResult different = new UploadCertificateResult("Other", "large", "small");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, different);
    }
}
