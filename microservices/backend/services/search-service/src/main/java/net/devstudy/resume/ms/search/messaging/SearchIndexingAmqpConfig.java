package net.devstudy.resume.ms.search.messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;

import net.devstudy.resume.search.api.messaging.SearchIndexingMessaging;

@Configuration
@ConditionalOnProperty(name = {
        "app.search.indexing.enabled",
        "app.search.indexing.messaging.enabled"
}, havingValue = "true")
public class SearchIndexingAmqpConfig {

    @Bean
    public TopicExchange profileSearchExchange() {
        return new TopicExchange(SearchIndexingMessaging.EXCHANGE, true, false);
    }

    @Bean
    public Queue searchIndexingQueue() {
        return QueueBuilder.durable(SearchIndexingMessaging.QUEUE).build();
    }

    @Bean
    public Binding searchIndexingBinding(Queue searchIndexingQueue, TopicExchange profileSearchExchange) {
        return BindingBuilder.bind(searchIndexingQueue)
                .to(profileSearchExchange)
                .with("profile.*");
    }
}
