package net.devstudy.resume.profile.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import net.devstudy.resume.profile.entity.Profile;
import net.devstudy.resume.profile.repository.storage.ProfileRepository;

@Configuration
@EntityScan(basePackageClasses = Profile.class)
@EnableJpaRepositories(basePackageClasses = ProfileRepository.class)
public class ProfileJpaConfig {
}
