package net.devstudy.resume.controller;

import net.devstudy.resume.entity.Profile;
import net.devstudy.resume.model.CurrentProfile;
import net.devstudy.resume.service.ProfileService;
import net.devstudy.resume.util.SecurityUtil;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import lombok.RequiredArgsConstructor;

import static net.devstudy.resume.Constants.UI.MAX_PROFILES_PER_PAGE;

import java.util.Optional;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class PublicDataController {
    private final ProfileService profileService;

    

    @GetMapping("/")
    public String redirectToWelcome() {
        return "redirect:/welcome";
    }

    @GetMapping("/welcome")
    public String listAll(Model model) {
        Page<Profile> page = profileService.findAll(
                PageRequest.of(0, MAX_PROFILES_PER_PAGE, Sort.by("id")));
        model.addAttribute("profiles", page.getContent());
        model.addAttribute("page", page);
        return "welcome";
    }

    @GetMapping("/{uid}")
    public String profile(@PathVariable String uid, Model model) {
        Optional<Profile> profileOptional = profileService.findByUid(uid);
        if (profileOptional.isEmpty()) {
            return "error/profile-not-found";
        }

        Profile profile = profileOptional.get();

        if (!profile.isCompleted()) {
            CurrentProfile currentProfile = SecurityUtil.getCurrentProfile();
            if (currentProfile == null || !currentProfile.getId().equals(profile.getId())) {
                return "error/profile-not-found";
            }
            return "redirect:/edit";
        }

        model.addAttribute("profile", profile);
        return "profile";
    }
}
