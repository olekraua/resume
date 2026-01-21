package net.devstudy.resume.search.repository.search;

import net.devstudy.resume.search.ProfileSearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProfileSearchRepository extends ElasticsearchRepository<ProfileSearchDocument, Long> {
}
