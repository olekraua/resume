package net.devstudy.resume.service;

import net.devstudy.resume.domain.Profile;
import net.devstudy.resume.form.SignUpForm;

public interface EditProfileService {
    Profile createNewProfile(SignUpForm form);
}
