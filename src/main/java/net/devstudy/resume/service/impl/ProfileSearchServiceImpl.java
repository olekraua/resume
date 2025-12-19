package net.devstudy.resume.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import lombok.RequiredArgsConstructor;
import net.devstudy.resume.entity.Profile;
import net.devstudy.resume.repository.search.ProfileSearchRepository;
import net.devstudy.resume.repository.storage.ProfileRepository;
import net.devstudy.resume.search.ProfileSearchDocument;
import net.devstudy.resume.service.ProfileSearchMapper;
import net.devstudy.resume.service.ProfileSearchService;

@Service
@ConditionalOnProperty(name = "app.search.elasticsearch.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class ProfileSearchServiceImpl implements ProfileSearchService {

    private final ProfileRepository profileRepository;
    private final ProfileSearchRepository profileSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ProfileSearchMapper profileSearchMapper;

    @Override
    public Page<Profile> search(String query, Pageable pageable) {
        String q = query == null ? "" : query.trim();
        if (q.length() < 2) {
            // занадто короткий запит: повертаємо всі профілі без фільтра
            return profileRepository.findAll(pageable);
        }
        // multi_match по основних текстових полях
        NativeQuery esQuery = NativeQuery.builder()
                .withQuery(b -> b.multiMatch(mm -> mm
                        .query(q)
                        .fields("fullName^3", "firstName^3", "lastName^3",
                                "summary.en", "summary.uk",
                                "objective.en", "objective.uk",
                                "info.en", "info.uk",
                                "skills.en", "skills.uk")
                        .type(TextQueryType.PhrasePrefix)))
                .withPageable(pageable)
                .build();

        SearchHits<ProfileSearchDocument> hits = elasticsearchOperations.search(esQuery, ProfileSearchDocument.class);
        if (hits.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> ids = hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(ProfileSearchDocument::getId)
                .toList();

        List<Profile> profiles = profileRepository.findAllById(ids);
        Map<Long, Profile> byId = profiles.stream()
                .collect(Collectors.toMap(Profile::getId, p -> p));

        List<Profile> ordered = ids.stream()
                .map(byId::get)
                .filter(p -> p != null)
                .toList();

        return new PageImpl<>(ordered, pageable, hits.getTotalHits());
    }

    @Override
    @Transactional(readOnly = true)
    public void reindexAll() {
        IndexOperations indexOperations = elasticsearchOperations.indexOps(ProfileSearchDocument.class);
        if (indexOperations.exists()) {
            indexOperations.delete();
        }
        indexOperations.createWithMapping();
        List<Profile> profiles = profileRepository.findAll(Pageable.unpaged()).getContent();
        indexProfiles(profiles);
    }

    @Override
    public void indexProfiles(List<Profile> profiles) {
        if (profiles == null || profiles.isEmpty()) {
            return;
        }
        List<ProfileSearchDocument> docs = profiles.stream()
                .map(profileSearchMapper::toDocument)
                .toList();
        profileSearchRepository.saveAll(docs);
    }
}
