package net.devstudy.resume.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import net.devstudy.resume.auth.config.AuthJpaConfig;
import net.devstudy.resume.profile.config.ProfileJpaConfig;
import net.devstudy.resume.staticdata.config.StaticDataJpaConfig;

@Configuration
@Import({
		AuthJpaConfig.class,
		ProfileJpaConfig.class,
		StaticDataJpaConfig.class
})
public class RepositoryConfig {
}
