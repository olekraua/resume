package net.devstudy.resume.search.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import net.devstudy.resume.search.service.ProfileSearchService;

class ElasticsearchIndexConfigTest {

    @Test
    void createsCommandLineRunnerWhenElasticsearchEnabled() {
        ProfileSearchService profileSearchService = Mockito.mock(ProfileSearchService.class);
        new ApplicationContextRunner()
                .withUserConfiguration(ElasticsearchIndexConfig.class)
                .withBean(ProfileSearchService.class, () -> profileSearchService)
                .withPropertyValues("app.search.elasticsearch.enabled=true")
                .run(context -> {
                    Map<String, CommandLineRunner> runners = context.getBeansOfType(CommandLineRunner.class);
                    assertTrue(runners.containsKey("indexProfiles"));
                });
    }

    @Test
    void doesNotCreateCommandLineRunnerWhenElasticsearchDisabled() {
        ProfileSearchService profileSearchService = Mockito.mock(ProfileSearchService.class);
        new ApplicationContextRunner()
                .withUserConfiguration(ElasticsearchIndexConfig.class)
                .withBean(ProfileSearchService.class, () -> profileSearchService)
                .withPropertyValues("app.search.elasticsearch.enabled=false")
                .run(context -> {
                    Map<String, CommandLineRunner> runners = context.getBeansOfType(CommandLineRunner.class);
                    assertTrue(runners.isEmpty());
                });
    }

    @Test
    void invokesReindexAllAndSwallowsErrors() {
        ProfileSearchService profileSearchService = Mockito.mock(ProfileSearchService.class);
        Mockito.doThrow(new RuntimeException("boom")).when(profileSearchService).reindexAll();

        new ApplicationContextRunner()
                .withUserConfiguration(ElasticsearchIndexConfig.class)
                .withBean(ProfileSearchService.class, () -> profileSearchService)
                .withPropertyValues("app.search.elasticsearch.enabled=true")
                .run(context -> {
                    CommandLineRunner runner = context.getBean("indexProfiles", CommandLineRunner.class);
                    assertDoesNotThrow(() -> runner.run());
                    Mockito.verify(profileSearchService).reindexAll();
                });
    }
}
