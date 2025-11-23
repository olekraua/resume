package net.devstudy.resume.service.impl;

import net.devstudy.resume.entity.Profile;
import net.devstudy.resume.repository.storage.ProfileRepository;
import net.devstudy.resume.service.ProfileService;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;



@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileServiceImpl implements ProfileService {
 
    private final ProfileRepository profileRepository;


    

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
}
