package net.devstudy.resume.profile.internal.messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import net.devstudy.resume.profile.api.event.ProfileIndexingRequestedEvent;
import net.devstudy.resume.profile.api.event.ProfilePasswordChangedEvent;
import net.devstudy.resume.profile.api.event.ProfileSearchRemovalRequestedEvent;
import net.devstudy.resume.shared.event.ProfileMediaCleanupRequestedEvent;
import net.devstudy.resume.shared.messaging.KafkaTopics;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class ProfileKafkaPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onProfileIndexing(ProfileIndexingRequestedEvent event) {
        if (event == null || event.snapshot() == null || event.snapshot().profileId() == null) {
            return;
        }
        String key = event.snapshot().profileId().toString();
        kafkaTemplate.send(KafkaTopics.PROFILE_INDEXING, key, event.snapshot());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onProfileRemoved(ProfileSearchRemovalRequestedEvent event) {
        if (event == null || event.profileId() == null) {
            return;
        }
        String key = event.profileId().toString();
        kafkaTemplate.send(KafkaTopics.PROFILE_REMOVED, key, event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onProfilePasswordChanged(ProfilePasswordChangedEvent event) {
        if (event == null || event.profileId() == null) {
            return;
        }
        String key = event.profileId().toString();
        kafkaTemplate.send(KafkaTopics.PROFILE_PASSWORD_CHANGED, key, event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onProfileMediaCleanup(ProfileMediaCleanupRequestedEvent event) {
        if (event == null) {
            return;
        }
        kafkaTemplate.send(KafkaTopics.PROFILE_MEDIA_CLEANUP, "media", event);
    }
}
