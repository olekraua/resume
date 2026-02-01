package net.devstudy.resume.search.internal.messaging;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import net.devstudy.resume.profile.api.event.ProfileIndexingSnapshot;
import net.devstudy.resume.profile.api.event.ProfileSearchRemovalRequestedEvent;
import net.devstudy.resume.profile.api.model.Profile;
import net.devstudy.resume.profile.api.model.Skill;
import net.devstudy.resume.search.internal.document.ProfileSearchDocument;
import net.devstudy.resume.search.internal.mapper.ProfileSearchMapper;
import net.devstudy.resume.search.internal.repository.search.ProfileSearchRepository;
import net.devstudy.resume.shared.messaging.KafkaTopics;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
@ConditionalOnProperty(name = "app.search.elasticsearch.enabled", havingValue = "true", matchIfMissing = true)
public class ProfileIndexingKafkaListener {

    private final ProfileSearchRepository profileSearchRepository;
    private final ProfileSearchMapper profileSearchMapper;

    @KafkaListener(topics = KafkaTopics.PROFILE_INDEXING)
    public void onProfileIndexing(ProfileIndexingSnapshot snapshot) {
        if (snapshot == null || snapshot.profileId() == null) {
            return;
        }
        Profile profile = toProfile(snapshot);
        ProfileSearchDocument doc = profileSearchMapper.toDocument(profile);
        profileSearchRepository.saveAll(List.of(doc));
    }

    @KafkaListener(topics = KafkaTopics.PROFILE_REMOVED)
    public void onProfileRemoved(ProfileSearchRemovalRequestedEvent event) {
        if (event == null || event.profileId() == null) {
            return;
        }
        profileSearchRepository.deleteById(event.profileId());
    }

    private Profile toProfile(ProfileIndexingSnapshot snapshot) {
        Profile profile = new Profile();
        profile.setId(snapshot.profileId());
        profile.setUid(snapshot.uid());
        profile.setFirstName(snapshot.firstName());
        profile.setLastName(snapshot.lastName());
        profile.setCity(snapshot.city());
        profile.setCountry(snapshot.country());
        profile.setSmallPhoto(snapshot.smallPhoto());
        if (snapshot.birthDay() != null) {
            profile.setBirthDay(java.sql.Date.valueOf(snapshot.birthDay()));
        }
        profile.setObjective(snapshot.objective());
        profile.setSummary(snapshot.summary());
        profile.setInfo(snapshot.info());
        profile.setSkills(toSkills(snapshot.skills()));
        return profile;
    }

    private java.util.List<Skill> toSkills(java.util.List<String> values) {
        if (values == null || values.isEmpty()) {
            return java.util.List.of();
        }
        java.util.List<Skill> skills = new java.util.ArrayList<>(values.size());
        for (String value : values) {
            Skill skill = new Skill();
            skill.setValue(value);
            skills.add(skill);
        }
        return skills;
    }
}
