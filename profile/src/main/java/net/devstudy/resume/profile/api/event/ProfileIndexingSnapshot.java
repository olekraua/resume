package net.devstudy.resume.profile.api.event;

import java.util.List;

public record ProfileIndexingSnapshot(Long profileId, String uid, String firstName, String lastName,
        String objective, String summary, String info, List<String> skills) {

    public ProfileIndexingSnapshot {
        skills = skills == null ? List.of() : List.copyOf(skills);
    }
}
