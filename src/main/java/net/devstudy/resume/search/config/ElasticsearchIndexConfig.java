package net.devstudy.resume.search.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.devstudy.resume.search.service.ProfileSearchService;

@Configuration
@ConditionalOnProperty(name = "app.search.elasticsearch.enabled", havingValue = "true", matchIfMissing = true)
public class ElasticsearchIndexConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchIndexConfig.class);

    @Bean
    @SuppressWarnings("unused")
    CommandLineRunner indexProfiles(ProfileSearchService profileSearchService) {
        return args -> {
            try {
                LOGGER.info("Reindexing profiles into Elasticsearch...");
                profileSearchService.reindexAll();
                LOGGER.info("Reindexing done");
            } catch (Exception e) {
                LOGGER.warn("Elasticsearch reindex failed: {}", e.getMessage());
            }
        };
    }
}
