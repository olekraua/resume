package net.devstudy.resume.auth.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import net.devstudy.resume.auth.entity.RememberMeToken;
import net.devstudy.resume.auth.repository.storage.RememberMeTokenRepository;

@Configuration
@EntityScan(basePackageClasses = RememberMeToken.class)
@EnableJpaRepositories(basePackageClasses = RememberMeTokenRepository.class)
public class AuthJpaConfig {
}
