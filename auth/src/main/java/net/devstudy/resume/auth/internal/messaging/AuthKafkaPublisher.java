package net.devstudy.resume.auth.internal.messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import net.devstudy.resume.notification.api.event.RestoreAccessMailRequestedEvent;
import net.devstudy.resume.shared.messaging.KafkaTopics;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class AuthKafkaPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onRestoreMailRequested(RestoreAccessMailRequestedEvent event) {
        if (event == null) {
            return;
        }
        kafkaTemplate.send(KafkaTopics.AUTH_RESTORE_MAIL_REQUESTED, event.email(), event);
    }
}
