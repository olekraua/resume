package net.devstudy.resume.media.internal.messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import net.devstudy.resume.media.api.service.MediaCleanupService;
import net.devstudy.resume.shared.event.ProfileMediaCleanupRequestedEvent;
import net.devstudy.resume.shared.messaging.KafkaTopics;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class ProfileMediaCleanupKafkaListener {

    private final MediaCleanupService mediaCleanupService;

    @KafkaListener(topics = KafkaTopics.PROFILE_MEDIA_CLEANUP)
    public void onProfileMediaCleanupRequested(ProfileMediaCleanupRequestedEvent event) {
        if (event == null) {
            return;
        }
        if (event.clearCertificateTempLinks()) {
            mediaCleanupService.clearCertificateTempLinks();
        }
        if (event.photoUrls() != null && !event.photoUrls().isEmpty()) {
            mediaCleanupService.removePhotos(event.photoUrls());
        }
        if (event.certificateUrls() != null && !event.certificateUrls().isEmpty()) {
            mediaCleanupService.removeCertificates(event.certificateUrls());
        }
    }
}
