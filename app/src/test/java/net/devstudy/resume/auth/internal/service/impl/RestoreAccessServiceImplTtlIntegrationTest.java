package net.devstudy.resume.auth.internal.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import net.devstudy.resume.profile.api.model.Profile;
import net.devstudy.resume.auth.internal.entity.ProfileRestore;
import net.devstudy.resume.profile.internal.repository.storage.ProfileRepository;
import net.devstudy.resume.auth.internal.repository.storage.ProfileRestoreRepository;
import net.devstudy.resume.auth.api.service.RestoreAccessService;
import net.devstudy.resume.testcontainers.PostgresIntegrationTest;

@TestPropertySource(properties = "app.restore.token-ttl=PT1H")
class RestoreAccessServiceImplTtlIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private RestoreAccessService restoreAccessService;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ProfileRestoreRepository profileRestoreRepository;

    @Test
    void expiresTokenWhenFetchingProfile() {
        Profile profile = createProfile("ttl-user-1");
        String token = "expired-token-1";
        Instant created = Instant.now().minus(Duration.ofHours(2));
        saveRestore(profile, token, created);

        Optional<Profile> resolved = restoreAccessService.findProfileByToken(token);

        assertTrue(resolved.isEmpty());
        assertTrue(profileRestoreRepository.findByProfileId(profile.getId()).isEmpty());
    }

    @Test
    void expiresTokenWhenResettingPassword() {
        Profile profile = createProfile("ttl-user-2");
        String token = "expired-token-2";
        Instant created = Instant.now().minus(Duration.ofHours(2));
        saveRestore(profile, token, created);

        assertThrows(IllegalArgumentException.class,
                () -> restoreAccessService.resetPassword(token, "newPass123"));

        assertTrue(profileRestoreRepository.findByProfileId(profile.getId()).isEmpty());
    }

    @Test
    void acceptsFreshToken() {
        Profile profile = createProfile("ttl-user-3");
        String token = "fresh-token-1";
        Instant created = Instant.now();
        saveRestore(profile, token, created);

        Optional<Profile> resolved = restoreAccessService.findProfileByToken(token);

        assertTrue(resolved.isPresent());
        assertEquals(profile.getId(), resolved.get().getId());
        assertFalse(profileRestoreRepository.findByProfileId(profile.getId()).isEmpty());
    }

    private Profile createProfile(String uid) {
        Profile profile = new Profile();
        profile.setUid(uid);
        profile.setFirstName("Test");
        profile.setLastName("User");
        profile.setPassword("password");
        profile.setCompleted(false);
        return profileRepository.save(profile);
    }

    private void saveRestore(Profile profile, String token, Instant created) {
        ProfileRestore restore = new ProfileRestore();
        restore.setProfile(profile);
        restore.setToken(token);
        restore.setCreated(created);
        profileRestoreRepository.save(restore);
    }
}
