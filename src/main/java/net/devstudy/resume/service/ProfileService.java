package net.devstudy.resume.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import net.devstudy.resume.entity.Profile;

public interface ProfileService {
    Optional<Profile> findByUid(String uid);

    Page<Profile> findAll(Pageable pageable);

    Iterable<Profile> findAllForIndexing();

    void updatePassword(Long profileId, String rawPassword);
}
