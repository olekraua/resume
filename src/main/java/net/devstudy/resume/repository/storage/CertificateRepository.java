package net.devstudy.resume.repository.storage;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import net.devstudy.resume.entity.Certificate;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByProfileId(Long profileId);

    void deleteByProfileId(Long profileId);
}
