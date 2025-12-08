package net.devstudy.resume.service;

import net.devstudy.resume.entity.Profile;
import net.devstudy.resume.search.ProfileSearchDocument;

public interface ProfileSearchMapper {
    ProfileSearchDocument toDocument(Profile profile);
}
