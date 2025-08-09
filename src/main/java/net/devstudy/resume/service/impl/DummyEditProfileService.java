package net.devstudy.resume.service.impl;

import net.devstudy.resume.domain.Profile;
import net.devstudy.resume.form.SignUpForm;
import net.devstudy.resume.service.EditProfileService;
import org.springframework.stereotype.Service;

@Service
public class DummyEditProfileService implements EditProfileService {
    @Override public Profile createNewProfile(SignUpForm form) { return new Profile(1L, true); }
}
