package net.devstudy.resume.controller;

import net.devstudy.resume.domain.Profile;
import net.devstudy.resume.service.FindProfileService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import static net.devstudy.resume.Constants.UI.MAX_PROFILES_PER_PAGE;

@Controller
public class PublicDataController {
    private final FindProfileService findProfileService;

    public PublicDataController(FindProfileService findProfileService) {
        this.findProfileService = findProfileService;
    }

    @GetMapping({"/", "/welcome"})
    public String showProfiles(Model model) {
        Page<Profile> profilesPage = findProfileService.findAll(
                PageRequest.of(0, MAX_PROFILES_PER_PAGE, Sort.by("id"))
        );
        model.addAttribute("profiles", profilesPage.getContent());
        model.addAttribute("page", profilesPage);
        return "welcome";
    }
}
