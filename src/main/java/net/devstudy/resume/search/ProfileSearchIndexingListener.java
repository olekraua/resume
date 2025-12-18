package net.devstudy.resume.search;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import net.devstudy.resume.entity.Profile;
import net.devstudy.resume.event.ProfileIndexingRequestedEvent;
import net.devstudy.resume.repository.storage.ProfileRepository;
import net.devstudy.resume.service.ProfileSearchService;

@Component
@RequiredArgsConstructor
public class ProfileSearchIndexingListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileSearchIndexingListener.class);

    private final ProfileRepository profileRepository;
    private final ProfileSearchService profileSearchService;

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProfileIndexingRequested(ProfileIndexingRequestedEvent event) {
        if (event == null || event.profileId() == null) {
            return;
        }
        try {
            profileRepository.findById(event.profileId()).ifPresent(profile -> {
                initializeCollections(profile);
                profileSearchService.indexProfiles(List.of(profile));
            });
        } catch (Exception ex) {
            LOGGER.warn("Elasticsearch index update failed: {}", ex.getMessage());
        }
    }

    private void initializeCollections(Profile profile) {
        if (profile.getSkills() != null) {
            profile.getSkills().size();
        }
    }
}
