package net.devstudy.resume.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class AppInfoConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(AppInfoConfig.class);

    @Test
    void createsInfoContributorWithDefaults() {
        contextRunner.run(context -> {
            InfoContributor contributor = context.getBean(InfoContributor.class);
            Info.Builder builder = new Info.Builder();
            contributor.contribute(builder);

            @SuppressWarnings("unchecked")
            Map<String, Object> app = (Map<String, Object>) builder.build().getDetails().get("app");

            assertEquals("resume", app.get("name"));
            assertEquals(List.of(), app.get("profiles"));
            assertEquals(Boolean.TRUE, app.get("elasticsearchEnabled"));
        });
    }

    @Test
    void injectsEnvironmentAndPropertiesIntoInfoContributor() {
        contextRunner
                .withPropertyValues(
                        "spring.application.name=my-resume",
                        "spring.profiles.active=dev,test",
                        "app.search.elasticsearch.enabled=false")
                .run(context -> {
                    InfoContributor contributor = context.getBean(InfoContributor.class);
                    Info.Builder builder = new Info.Builder();
                    contributor.contribute(builder);

                    @SuppressWarnings("unchecked")
                    Map<String, Object> app = (Map<String, Object>) builder.build().getDetails().get("app");

                    assertEquals("my-resume", app.get("name"));
                    assertEquals(List.of("dev", "test"), app.get("profiles"));
                    assertEquals(Boolean.FALSE, app.get("elasticsearchEnabled"));
                });
    }
}

