package net.devstudy.resume.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import net.devstudy.resume.entity.Profile;

public interface ProfileService {
    Profile findByUid(String uid);

    Page<Profile> findAll(Pageable pageable);

    Iterable<Profile> findAllForIndexing();

}
