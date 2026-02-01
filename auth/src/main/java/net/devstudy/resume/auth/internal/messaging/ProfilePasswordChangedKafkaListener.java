package net.devstudy.resume.auth.internal.messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import net.devstudy.resume.auth.internal.repository.storage.RememberMeTokenRepository;
import net.devstudy.resume.profile.api.event.ProfilePasswordChangedEvent;
import net.devstudy.resume.shared.messaging.KafkaTopics;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class ProfilePasswordChangedKafkaListener {

    private final RememberMeTokenRepository rememberMeTokenRepository;

    @KafkaListener(topics = KafkaTopics.PROFILE_PASSWORD_CHANGED)
    public void onProfilePasswordChanged(ProfilePasswordChangedEvent event) {
        if (event == null || event.profileId() == null) {
            return;
        }
        rememberMeTokenRepository.deleteByProfileId(event.profileId());
    }
}
