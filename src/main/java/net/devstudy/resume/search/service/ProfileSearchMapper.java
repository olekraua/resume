package net.devstudy.resume.search.service;

import net.devstudy.resume.profile.entity.Profile;
import net.devstudy.resume.search.ProfileSearchDocument;

public interface ProfileSearchMapper {
    ProfileSearchDocument toDocument(Profile profile);
}
