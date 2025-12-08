package net.devstudy.resume.repository.search;

import net.devstudy.resume.search.ProfileSearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProfileSearchRepository extends ElasticsearchRepository<ProfileSearchDocument, Long> {
}
