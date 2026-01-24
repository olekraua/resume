package net.devstudy.resume.auth.internal.repository.storage;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import net.devstudy.resume.auth.internal.entity.RememberMeToken;

public interface RememberMeTokenRepository extends JpaRepository<RememberMeToken, String> {

    @EntityGraph(attributePaths = "profile")
    Optional<RememberMeToken> findBySeries(String series);

    long deleteByProfileId(Long profileId);

    long deleteByProfileUid(String uid);
}
