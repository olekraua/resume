 package net.devstudy.resume.service;

import net.devstudy.resume.domain.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FindProfileService {
    Profile findByUid(String uid);
    Page<Profile> findAll(Pageable pageable);
    Page<Profile> searchProfiles(String query, Pageable pageable);
}

