package net.devstudy.resume.auth.service;

import java.util.Optional;

import net.devstudy.resume.profile.entity.Profile;

public interface RestoreAccessService {

    String requestRestore(String identifier, String appHost);

    Optional<Profile> findProfileByToken(String token);

    void resetPassword(String token, String rawPassword);
}
