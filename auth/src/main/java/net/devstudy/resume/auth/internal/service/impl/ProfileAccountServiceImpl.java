package net.devstudy.resume.auth.internal.service.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import net.devstudy.resume.auth.api.service.ProfileAccountService;
import net.devstudy.resume.auth.internal.client.ProfileInternalClient;
import net.devstudy.resume.profile.api.dto.internal.ProfileAuthResponse;
import net.devstudy.resume.profile.api.dto.internal.ProfilePasswordUpdateRequest;
import net.devstudy.resume.profile.api.dto.internal.ProfileRegistrationRequest;
import net.devstudy.resume.profile.api.dto.internal.ProfileUidUpdateRequest;

@Service
@RequiredArgsConstructor
public class ProfileAccountServiceImpl implements ProfileAccountService {

    private final ProfileInternalClient profileInternalClient;

    @Override
    public ProfileAuthResponse register(ProfileRegistrationRequest request) {
        return profileInternalClient.register(request);
    }

    @Override
    public ProfileAuthResponse loadForAuth(String uid) {
        return profileInternalClient.loadForAuth(uid);
    }

    @Override
    public void updatePassword(Long profileId, ProfilePasswordUpdateRequest request) {
        profileInternalClient.updatePassword(profileId, request);
    }

    @Override
    public void updateUid(Long profileId, ProfileUidUpdateRequest request) {
        profileInternalClient.updateUid(profileId, request);
    }

    @Override
    public void removeProfile(Long profileId) {
        profileInternalClient.removeProfile(profileId);
    }
}
