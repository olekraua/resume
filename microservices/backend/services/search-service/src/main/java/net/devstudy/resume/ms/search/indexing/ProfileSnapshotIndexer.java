package net.devstudy.resume.ms.search.indexing;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import net.devstudy.resume.profile.api.event.ProfileIndexingSnapshot;
import net.devstudy.resume.profile.api.model.Profile;
import net.devstudy.resume.profile.api.model.Skill;
import net.devstudy.resume.search.internal.document.ProfileSearchDocument;
import net.devstudy.resume.search.internal.mapper.ProfileSearchMapper;
import net.devstudy.resume.search.internal.repository.search.ProfileSearchRepository;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.search.indexing.enabled", havingValue = "true")
public class ProfileSnapshotIndexer {

    private final ProfileSearchRepository profileSearchRepository;
    private final ProfileSearchMapper profileSearchMapper;

    public void index(ProfileIndexingSnapshot snapshot) {
        if (snapshot == null || snapshot.profileId() == null) {
            return;
        }
        Profile profile = toProfile(snapshot);
        ProfileSearchDocument document = profileSearchMapper.toDocument(profile);
        profileSearchRepository.save(document);
    }

    public void remove(Long profileId) {
        if (profileId == null) {
            return;
        }
        profileSearchRepository.deleteById(profileId);
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
            profile.setBirthDay(Date.valueOf(snapshot.birthDay()));
        }
        profile.setObjective(snapshot.objective());
        profile.setSummary(snapshot.summary());
        profile.setInfo(snapshot.info());
        profile.setSkills(toSkills(snapshot.skills()));
        return profile;
    }

    private List<Skill> toSkills(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        List<Skill> skills = new ArrayList<>(values.size());
        for (String value : values) {
            Skill skill = new Skill();
            skill.setValue(value);
            skills.add(skill);
        }
        return skills;
    }
}
