package net.devstudy.resume.auth.repository.storage;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.devstudy.resume.auth.entity.ProfileRestore;

public interface ProfileRestoreRepository extends JpaRepository<ProfileRestore, Long> {

    Optional<ProfileRestore> findByToken(String token);

    Optional<ProfileRestore> findByProfileId(Long profileId);

    void deleteByProfileId(Long profileId);
}
