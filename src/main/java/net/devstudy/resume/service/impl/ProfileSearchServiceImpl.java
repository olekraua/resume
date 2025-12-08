package net.devstudy.resume.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import net.devstudy.resume.entity.Profile;
import net.devstudy.resume.entity.Skill;
import net.devstudy.resume.repository.search.ProfileSearchRepository;
import net.devstudy.resume.repository.storage.ProfileRepository;
import net.devstudy.resume.search.ProfileSearchDocument;
import net.devstudy.resume.service.ProfileSearchService;

@Service
@RequiredArgsConstructor
public class ProfileSearchServiceImpl implements ProfileSearchService {

    private final ProfileRepository profileRepository;
    private final ProfileSearchRepository profileSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public Page<Profile> search(String query, Pageable pageable) {
        // multi_match по основних текстових полях
        NativeQuery esQuery = NativeQuery.builder()
                .withQuery(q -> q.multiMatch(mm -> mm
                        .query(query)
                        .fields("fullName^2", "summary", "objective", "skills")
                        .fuzziness("AUTO")))
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
        profileSearchRepository.deleteAll();
        List<Profile> profiles = profileRepository.findAllByCompletedTrue(Pageable.unpaged()).getContent();
        indexProfiles(profiles);
    }

    @Override
    public void indexProfiles(List<Profile> profiles) {
        if (profiles == null || profiles.isEmpty()) {
            return;
        }
        List<ProfileSearchDocument> docs = profiles.stream()
                .map(this::toDocument)
                .toList();
        profileSearchRepository.saveAll(docs);
    }

    private ProfileSearchDocument toDocument(Profile profile) {
        String fullName = (profile.getFirstName() == null ? "" : profile.getFirstName()) + " "
                + (profile.getLastName() == null ? "" : profile.getLastName());
        String skills = extractSkills(profile.getSkills());
        return new ProfileSearchDocument(profile.getId(), profile.getUid(), fullName.trim(),
                safe(profile.getObjective()), safe(profile.getSummary()), skills);
    }

    private String extractSkills(List<Skill> skills) {
        if (skills == null || skills.isEmpty()) {
            return "";
        }
        return skills.stream()
                .map(Skill::getValue)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(", "));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
