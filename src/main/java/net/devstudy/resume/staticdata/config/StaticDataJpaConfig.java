package net.devstudy.resume.staticdata.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import net.devstudy.resume.staticdata.entity.SkillCategory;
import net.devstudy.resume.staticdata.repository.storage.SkillCategoryRepository;

@Configuration
@EntityScan(basePackageClasses = SkillCategory.class)
@EnableJpaRepositories(basePackageClasses = SkillCategoryRepository.class)
public class StaticDataJpaConfig {
}
