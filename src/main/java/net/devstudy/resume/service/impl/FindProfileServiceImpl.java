package net.devstudy.resume.service.impl;

import net.devstudy.resume.domain.Profile;
import net.devstudy.resume.repository.ProfileRepository;
import net.devstudy.resume.service.FindProfileService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class FindProfileServiceImpl implements FindProfileService {
    private final ProfileRepository repo;

    // Конструкторна інʼєкція без Lombok
    public FindProfileServiceImpl(ProfileRepository repo) {
        this.repo = repo;
    }

    @Override public Profile findByUid(String uid) {
        return repo.findByUid(uid).orElse(null);
    }

    @Override public Page<Profile> findAll(Pageable pageable) {
        return repo.findAll(pageable);
    }

    @Override public Page<Profile> findBySearchQuery(String query, Pageable pageable) {
        return repo.search(query, pageable);
    }
}
