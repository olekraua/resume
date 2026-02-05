package net.devstudy.resume.ms.search.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import net.devstudy.resume.profile.api.event.ProfileIndexingSnapshot;
import net.devstudy.resume.search.api.messaging.SearchIndexingMessaging;
import net.devstudy.resume.ms.search.indexing.ProfileSnapshotIndexer;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.search.indexing.enabled", havingValue = "true")
public class SearchIndexingConsumer {

    private final ObjectMapper objectMapper;
    private final ProfileSnapshotIndexer snapshotIndexer;

    @RabbitListener(queues = SearchIndexingMessaging.QUEUE)
    public void onMessage(String payload, @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey)
            throws Exception {
        if (routingKey == null || payload == null || payload.isBlank()) {
            return;
        }
        switch (routingKey) {
            case SearchIndexingMessaging.ROUTING_KEY_INDEX -> {
                ProfileIndexingSnapshot snapshot = objectMapper.readValue(payload, ProfileIndexingSnapshot.class);
                snapshotIndexer.index(snapshot);
            }
            case SearchIndexingMessaging.ROUTING_KEY_REMOVE -> {
                RemovalPayload removal = objectMapper.readValue(payload, RemovalPayload.class);
                snapshotIndexer.remove(removal.profileId());
            }
            default -> {
                // ignore unknown routing keys
            }
        }
    }

    public record RemovalPayload(Long profileId) {
    }
}
