package net.devstudy.resume.service.impl;

import java.util.Optional;

import net.devstudy.resume.entity.Profile;
import net.devstudy.resume.repository.storage.ProfileRepository;
import net.devstudy.resume.service.ProfileService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<Profile> findByUid(String uid) {
        return profileRepository.findByUid(uid);
    }

    @Override
    public Page<Profile> findAll(Pageable pageable) {
        return profileRepository.findAll(pageable);
    }

    @Override
    public Iterable<Profile> findAllForIndexing() {
        // тимчасово: просто повертаємо все з БД
        return profileRepository.findAll();
    }

    @Override
    @Transactional
    public void updatePassword(Long profileId, String rawPassword) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found: " + profileId));
        profile.setPassword(passwordEncoder.encode(rawPassword));
        profileRepository.save(profile);
    }

    @Override
    @Transactional
    public Profile register(String uid, String firstName, String lastName, String rawPassword) {
        if (profileRepository.findByUid(uid).isPresent()) {
            throw new IllegalArgumentException("Uid already exists: " + uid);
        }
        Profile profile = new Profile();
        profile.setUid(uid);
        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        profile.setPassword(passwordEncoder.encode(rawPassword));
        profile.setCompleted(false);
        return profileRepository.save(profile);
    }

    @Override
    public Optional<Profile> findById(Long id) {
        return profileRepository.findById(id);
    }

    @Override
    @Transactional
    public void updateUid(Long profileId, String newUid) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found: " + profileId));
        if (profileRepository.findByUid(newUid).isPresent()) {
            throw new IllegalArgumentException("Uid already exists: " + newUid);
        }
        profile.setUid(newUid);
        profileRepository.save(profile);
    }
}
