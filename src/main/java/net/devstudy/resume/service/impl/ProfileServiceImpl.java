package net.devstudy.resume.service.impl;

import net.devstudy.resume.domain.Profile;
import net.devstudy.resume.repository.storage.ProfileRepository;
import net.devstudy.resume.service.ProfileService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProfileServiceImpl implements ProfileService {
    private final ProfileRepository profileRepository;

    // Конструкторна інʼєкція без Lombok
    public ProfileServiceImpl(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public Profile findByUid(String uid) {
        return profileRepository.findByUid(uid).orElse(null);
    }

    @Override
    public Page<Profile> findAll(Pageable pageable) {
        return profileRepository.findAll(pageable);
    }

    @Override
    public Page<Profile> findCompleted(Pageable pageable) {
        return profileRepository.findCompleted(pageable);
    }
}
