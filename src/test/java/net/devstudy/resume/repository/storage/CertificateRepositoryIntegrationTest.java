package net.devstudy.resume.repository.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import net.devstudy.resume.entity.Certificate;
import net.devstudy.resume.entity.Profile;
import net.devstudy.resume.testcontainers.PostgresIntegrationTest;

@Transactional
class CertificateRepositoryIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Test
    void findByProfileIdReturnsOnlyMatchingCertificates() {
        Profile first = createProfile("user-one");
        Profile second = createProfile("user-two");

        Certificate cert1 = createCertificate(first, "1");
        Certificate cert2 = createCertificate(first, "2");
        createCertificate(second, "3");

        List<Certificate> result = certificateRepository.findByProfileId(first.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(cert -> cert.getId().equals(cert1.getId())));
        assertTrue(result.stream().anyMatch(cert -> cert.getId().equals(cert2.getId())));
    }

    @Test
    void deleteByProfileIdRemovesOnlyMatchingCertificates() {
        Profile first = createProfile("user-three");
        Profile second = createProfile("user-four");

        createCertificate(first, "1");
        createCertificate(first, "2");
        createCertificate(second, "3");

        certificateRepository.deleteByProfileId(first.getId());
        certificateRepository.flush();

        assertEquals(0, certificateRepository.findByProfileId(first.getId()).size());
        assertEquals(1, certificateRepository.findByProfileId(second.getId()).size());
        assertEquals(1, certificateRepository.count());
    }

    private Profile createProfile(String uid) {
        Profile profile = new Profile();
        profile.setUid(uid);
        profile.setFirstName("John");
        profile.setLastName("Doe");
        profile.setPassword("password");
        profile.setCompleted(true);
        return profileRepository.saveAndFlush(profile);
    }

    private Certificate createCertificate(Profile profile, String suffix) {
        Certificate certificate = new Certificate();
        certificate.setProfile(profile);
        certificate.setLargeUrl("large-" + suffix);
        certificate.setSmallUrl("small-" + suffix);
        certificate.setName("Cert " + suffix);
        certificate.setIssuer("Issuer " + suffix);
        return certificateRepository.saveAndFlush(certificate);
    }
}
