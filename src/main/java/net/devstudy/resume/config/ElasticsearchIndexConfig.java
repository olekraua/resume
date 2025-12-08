package net.devstudy.resume.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.devstudy.resume.service.ProfileSearchService;

@Configuration
public class ElasticsearchIndexConfig {
    private static final Logger log = LoggerFactory.getLogger(ElasticsearchIndexConfig.class);

    @Bean
    CommandLineRunner indexProfiles(ProfileSearchService profileSearchService) {
        return args -> {
            try {
                log.info("Reindexing profiles into Elasticsearch...");
                profileSearchService.reindexAll();
                log.info("Reindexing done");
            } catch (Exception e) {
                log.warn("Elasticsearch reindex failed: {}", e.getMessage());
            }
        };
    }
}
