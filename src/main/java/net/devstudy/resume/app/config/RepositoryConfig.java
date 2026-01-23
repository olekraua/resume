package net.devstudy.resume.app.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import net.devstudy.resume.auth.entity.RememberMeToken;
import net.devstudy.resume.auth.repository.storage.RememberMeTokenRepository;
import net.devstudy.resume.profile.entity.Profile;
import net.devstudy.resume.profile.repository.storage.ProfileRepository;
import net.devstudy.resume.staticdata.entity.SkillCategory;
import net.devstudy.resume.staticdata.repository.storage.SkillCategoryRepository;

@Configuration
@EntityScan(basePackageClasses = {
        Profile.class,
        RememberMeToken.class,
        SkillCategory.class
})
@EnableJpaRepositories(basePackageClasses = {
        ProfileRepository.class,
        RememberMeTokenRepository.class,
        SkillCategoryRepository.class
})
public class RepositoryConfig {
}
