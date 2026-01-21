package net.devstudy.resume.app.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = {
        "net.devstudy.resume.profile.entity",
        "net.devstudy.resume.auth.entity",
        "net.devstudy.resume.staticdata.entity"
})
@EnableJpaRepositories(basePackages = {
        "net.devstudy.resume.profile.repository.storage",
        "net.devstudy.resume.auth.repository.storage",
        "net.devstudy.resume.staticdata.repository.storage"
})
public class RepositoryConfig {
}
