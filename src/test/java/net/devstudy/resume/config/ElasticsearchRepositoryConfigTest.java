package net.devstudy.resume.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;

import net.devstudy.resume.repository.search.ProfileSearchRepository;
import net.devstudy.resume.search.ProfileSearchDocument;

class ElasticsearchRepositoryConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ElasticsearchRepositoryConfig.class);

    @Test
    void createsRepositoryWhenElasticsearchEnabled() {
        ElasticsearchOperations operations = mockOperations();
        contextRunner.withBean("elasticsearchTemplate", ElasticsearchOperations.class, () -> operations)
                .withPropertyValues("app.search.elasticsearch.enabled=true")
                .run(context -> {
                    Map<String, ProfileSearchRepository> repos = context.getBeansOfType(ProfileSearchRepository.class);
                    assertEquals(1, repos.size());
                });
    }

    @Test
    void doesNotCreateRepositoryWhenElasticsearchDisabled() {
        contextRunner.withPropertyValues("app.search.elasticsearch.enabled=false")
                .run(context -> {
                    Map<String, ProfileSearchRepository> repos = context.getBeansOfType(ProfileSearchRepository.class);
                    assertEquals(0, repos.size());
                });
    }

    private ElasticsearchOperations mockOperations() {
        SimpleElasticsearchMappingContext mappingContext = new SimpleElasticsearchMappingContext();
        MappingElasticsearchConverter converter = new MappingElasticsearchConverter(mappingContext);
        IndexOperations indexOperations = Mockito.mock(IndexOperations.class);
        Mockito.when(indexOperations.exists()).thenReturn(true);
        Mockito.when(indexOperations.createWithMapping()).thenReturn(true);
        Mockito.when(indexOperations.putMapping()).thenReturn(true);
        ElasticsearchOperations operations = Mockito.mock(ElasticsearchOperations.class);
        Mockito.when(operations.getElasticsearchConverter()).thenReturn(converter);
        Mockito.when(operations.indexOps(ProfileSearchDocument.class)).thenReturn(indexOperations);
        return operations;
    }
}
