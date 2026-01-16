package net.devstudy.resume.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = { "net.devstudy.resume.entity", "net.devstudy.resume.staticdata.entity" })
@EnableJpaRepositories(basePackages = { "net.devstudy.resume.repository.storage",
        "net.devstudy.resume.staticdata.repository" })
public class RepositoryConfig {
}
