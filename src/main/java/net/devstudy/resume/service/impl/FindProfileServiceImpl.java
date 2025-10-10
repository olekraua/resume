package net.devstudy.resume.service.impl;

import org.springframework.data.domain.Sort;

import net.devstudy.resume.component.DataBuilder;
import net.devstudy.resume.domain.Profile;
import net.devstudy.resume.domain.ProfileRestore;
import net.devstudy.resume.exception.CantCompleteClientRequestException;
import net.devstudy.resume.repository.search.ProfileSearchRepository;
import net.devstudy.resume.repository.storage.ProfileRepository;
import net.devstudy.resume.repository.storage.ProfileRestoreRepository;
import net.devstudy.resume.service.FindProfileService;
import net.devstudy.resume.service.NotificationManagerService;
import net.devstudy.resume.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class FindProfileServiceImpl implements FindProfileService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FindProfileServiceImpl.class);

    private final ProfileRepository profileRepository;
    private final ProfileSearchRepository profileSearchRepository;
    private final ProfileRestoreRepository profileRestoreRepository;
    private final NotificationManagerService notificationManagerService;
    protected final DataBuilder dataBuilder;

    @Value("${application.host}")
    private String appHost;

    public FindProfileServiceImpl(
            ProfileRepository profileRepository,
            ProfileSearchRepository profileSearchRepository,
            ProfileRestoreRepository profileRestoreRepository,
            NotificationManagerService notificationManagerService,
            DataBuilder dataBuilder
    ) {
        this.profileRepository = profileRepository;
        this.profileSearchRepository = profileSearchRepository;
        this.profileRestoreRepository = profileRestoreRepository;
        this.notificationManagerService = notificationManagerService;
        this.dataBuilder = dataBuilder;
    }

    @Override
    public Profile findByUid(String uid) {
        return profileRepository.findByUid(uid.toLowerCase());
    }

    @Override
    public Page<Profile> findAll(Pageable pageable) {
        return profileRepository.findAllByCompletedTrue(pageable);
    }

    @Override
public Page<Profile> findBySearchQuery(String query, Pageable pageable) {
    Pageable sorted = PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            Sort.by("uid").descending()
    );
    return profileSearchRepository.searchByQuery(query, sorted);
}


    @Override
    @Transactional
    public void restoreAccess(String anyUnigueId) {
        Profile profile = profileRepository.findByUidOrEmailOrPhone(anyUnigueId, anyUnigueId, anyUnigueId);
        if (profile != null) {
            ProfileRestore restore = profileRestoreRepository.findByProfileId(profile.getId());
            if (restore == null) {
                restore = new ProfileRestore();
                restore.setProfile(profile);
            }
            restore.setToken(SecurityUtil.generateNewRestoreAccessToken());
            profileRestoreRepository.save(restore);
            sentRestoreLinkNotificationIfTransactionSuccess(profile, restore);
        } else {
            LOGGER.error("Profile not found by anyIdAcount: {}", anyUnigueId);
        }
    }

    protected void sentRestoreLinkNotificationIfTransactionSuccess(final Profile profile, ProfileRestore restore) {
        final String restoreLink = dataBuilder.buildRestoreAccessLink(appHost, restore.getToken());
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                notificationManagerService.sendRestoreAccessLink(profile, restoreLink);
            }
        });
    }

    @Override
    @Transactional
    public Profile findByRestoreToken(String token) {
        ProfileRestore restore = profileRestoreRepository.findByToken(token);
        if (restore == null) {
            throw new CantCompleteClientRequestException("Invalid token");
        }
        profileRestoreRepository.delete(restore);
        return restore.getProfile();
    }

    @Override
    @Transactional
    public Iterable<Profile> findAllForIndexing() {
        return profileRepository.findAll();
    }
}
