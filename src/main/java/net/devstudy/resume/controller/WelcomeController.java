package net.devstudy.resume.controller;

import net.devstudy.resume.domain.Profile;
import net.devstudy.resume.service.ProfileService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import static net.devstudy.resume.Constants.UI.MAX_PROFILES_PER_PAGE;

@Controller
public class WelcomeController {
    private final ProfileService profileService;

    public WelcomeController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping({ "/", "/welcome" })
    public String showProfiles(Model model) {
        Page<Profile> page = profileService.findAll(
                PageRequest.of(0, MAX_PROFILES_PER_PAGE, Sort.by("id")));
        model.addAttribute("pagedProfiles", page.getContent());
        model.addAttribute("page", page);
        return "welcome";
    }
}