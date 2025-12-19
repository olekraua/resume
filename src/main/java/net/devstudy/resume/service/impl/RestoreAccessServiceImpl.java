package net.devstudy.resume.service.impl;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.devstudy.resume.component.DataBuilder;
import net.devstudy.resume.entity.Profile;
import net.devstudy.resume.entity.ProfileRestore;
import net.devstudy.resume.repository.storage.ProfileRepository;
import net.devstudy.resume.repository.storage.ProfileRestoreRepository;
import net.devstudy.resume.service.ProfileService;
import net.devstudy.resume.service.RestoreAccessService;

@Service
public class RestoreAccessServiceImpl implements RestoreAccessService {

    private final ProfileRepository profileRepository;
    private final ProfileRestoreRepository profileRestoreRepository;
    private final ProfileService profileService;
    private final DataBuilder dataBuilder;

    public RestoreAccessServiceImpl(ProfileRepository profileRepository,
            ProfileRestoreRepository profileRestoreRepository,
            ProfileService profileService,
            DataBuilder dataBuilder) {
        this.profileRepository = profileRepository;
        this.profileRestoreRepository = profileRestoreRepository;
        this.profileService = profileService;
        this.dataBuilder = dataBuilder;
    }

    @Override
    @Transactional
    public String requestRestore(String identifier, String appHost) {
        Profile profile = findProfileByIdentifier(identifier)
                .orElseThrow(() -> new IllegalArgumentException("Профіль не знайдено"));

        ProfileRestore restore = profileRestoreRepository.findByProfileId(profile.getId())
                .orElseGet(ProfileRestore::new);
        restore.setProfile(profile);
        restore.setToken(generateToken());
        restore.setCreated(Instant.now());
        profileRestoreRepository.save(restore);

        return dataBuilder.buildRestoreAccessLink(appHost, restore.getToken());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Profile> findProfileByToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return profileRestoreRepository.findByToken(token).map(ProfileRestore::getProfile);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String rawPassword) {
        ProfileRestore restore = profileRestoreRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Невірний токен відновлення"));
        profileService.updatePassword(restore.getProfile().getId(), rawPassword);
        profileRestoreRepository.delete(restore);
    }

    private Optional<Profile> findProfileByIdentifier(String identifier) {
        if (identifier == null) {
            return Optional.empty();
        }
        String trimmed = identifier.trim();
        if (trimmed.isEmpty()) {
            return Optional.empty();
        }
        String lower = trimmed.toLowerCase(Locale.ENGLISH);
        Optional<Profile> byUid = profileRepository.findByUid(lower);
        if (byUid.isPresent()) {
            return byUid;
        }
        Optional<Profile> byEmail = profileRepository.findByEmail(lower);
        if (byEmail.isPresent()) {
            return byEmail;
        }
        return profileRepository.findByPhone(trimmed);
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
