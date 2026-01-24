package net.devstudy.resume.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import net.devstudy.resume.auth.api.config.AuthJpaConfig;
import net.devstudy.resume.profile.api.config.ProfileJpaConfig;
import net.devstudy.resume.staticdata.api.config.StaticDataJpaConfig;

@Configuration
@Import({
		AuthJpaConfig.class,
		ProfileJpaConfig.class,
		StaticDataJpaConfig.class
})
public class RepositoryConfig {
}
