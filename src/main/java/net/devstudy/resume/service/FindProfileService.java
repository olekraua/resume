package net.devstudy.resume.service;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import net.devstudy.resume.domain.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FindProfileService {
    Profile findByUid(String uid);
    Page<Profile> findAll(Pageable pageable);
    Page<Profile> findBySearchQuery(String query, Pageable pageable);
    @Nullable Profile findByRestoreToken(@Nonnull String token);
}
