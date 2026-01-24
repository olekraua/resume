package net.devstudy.resume.app.config;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class AppInfoConfig {

	@Bean
	InfoContributor appInfoContributor(Environment environment,
			@Value("${app.search.elasticsearch.enabled:true}") boolean elasticsearchEnabled) {
		return builder -> builder.withDetail("app", Map.of(
				"name", environment.getProperty("spring.application.name", "resume"),
				"profiles", Arrays.asList(environment.getActiveProfiles()),
				"elasticsearchEnabled", elasticsearchEnabled));
	}
}
