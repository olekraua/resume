package net.devstudy.resume.service.impl;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import net.devstudy.resume.entity.Profile;
import net.devstudy.resume.entity.Skill;
import net.devstudy.resume.search.ProfileSearchDocument;
import net.devstudy.resume.service.ProfileSearchMapper;

@Component
public class ProfileSearchMapperImpl implements ProfileSearchMapper {

    @Override
    public ProfileSearchDocument toDocument(Profile profile) {
        String first = safe(profile.getFirstName());
        String last = safe(profile.getLastName());
        String fullName = (first + " " + last).trim();
        String skills = extractSkills(profile.getSkills());
        return new ProfileSearchDocument(profile.getId(), profile.getUid(), first, last, fullName,
                safe(profile.getObjective()), safe(profile.getSummary()), safe(profile.getInfo()), skills);
    }

    private String extractSkills(java.util.List<Skill> skills) {
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
