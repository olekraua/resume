package net.devstudy.resume.notification.internal.messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import net.devstudy.resume.notification.api.event.RestoreAccessMailRequestedEvent;
import net.devstudy.resume.notification.internal.service.RestoreAccessMailService;
import net.devstudy.resume.shared.messaging.KafkaTopics;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class RestoreAccessMailKafkaListener {

    private final RestoreAccessMailService restoreAccessMailService;

    @KafkaListener(topics = KafkaTopics.AUTH_RESTORE_MAIL_REQUESTED)
    public void onRestoreMailRequested(RestoreAccessMailRequestedEvent event) {
        if (event == null) {
            return;
        }
        restoreAccessMailService.sendRestoreLink(event.email(), event.firstName(), event.link());
    }
}
