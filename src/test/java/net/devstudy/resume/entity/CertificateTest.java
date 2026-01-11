package net.devstudy.resume.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CertificateTest {

    @Test
    void equalsReturnsTrueForSameInstance() {
        Certificate certificate = buildCertificate(1L, "large", "name", "small", "issuer");

        assertTrue(certificate.equals(certificate));
    }

    @Test
    void equalsReturnsFalseWhenIdsDiffer() {
        Certificate first = buildCertificate(1L, "large", "name", "small", "issuer");
        Certificate second = buildCertificate(2L, "large", "name", "small", "issuer");

        assertFalse(first.equals(second));
    }

    @Test
    void equalsReturnsFalseForDifferentAbstractEntityTypeWithSameId() {
        Certificate certificate = buildCertificate(1L, "large", "name", "small", "issuer");
        OtherEntity other = new OtherEntity(1L);

        assertFalse(certificate.equals(other));
    }

    @Test
    void equalsReturnsTrueWhenAllFieldsEqual() {
        Certificate first = buildCertificate(1L, "large", "name", "small", "issuer");
        Certificate second = buildCertificate(1L, "large", "name", "small", "issuer");

        assertNotSame(first, second);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void equalsReturnsTrueWhenIdMatchesEvenIfFieldsDiffer() {
        Certificate first = buildCertificate(1L, "large", "name", "small", "issuer");
        Certificate second = buildCertificate(1L, "large", "name", "small", "other");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    private static Certificate buildCertificate(Long id, String largeUrl, String name, String smallUrl,
            String issuer) {
        Certificate certificate = new Certificate();
        certificate.setId(id);
        certificate.setLargeUrl(largeUrl);
        certificate.setName(name);
        certificate.setSmallUrl(smallUrl);
        certificate.setIssuer(issuer);
        return certificate;
    }

    private static final class OtherEntity extends AbstractEntity<Long> {

        private final Long id;

        private OtherEntity(Long id) {
            this.id = id;
        }

        @Override
        public Long getId() {
            return id;
        }
    }
}
