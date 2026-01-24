package net.devstudy.resume.auth.api.service;

import java.util.Optional;

import net.devstudy.resume.profile.api.model.Profile;

public interface RestoreAccessService {

    String requestRestore(String identifier, String appHost);

    Optional<Profile> findProfileByToken(String token);

    void resetPassword(String token, String rawPassword);
}
