package net.devstudy.resume.search.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import net.devstudy.resume.search.repository.search.ProfileSearchRepository;

@Configuration
@ConditionalOnProperty(name = "app.search.elasticsearch.enabled", havingValue = "true", matchIfMissing = true)
@EnableElasticsearchRepositories(basePackageClasses = ProfileSearchRepository.class)
public class ElasticsearchRepositoryConfig {
}
