package net.devstudy.resume.controller;

import net.devstudy.resume.entity.Profile;
import net.devstudy.resume.service.ProfileService;

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

    

    @GetMapping({ "/", "/welcome" })
    public String listAll(Model model) {
        Page<Profile> page = profileService.findAll(
                PageRequest.of(0, MAX_PROFILES_PER_PAGE, Sort.by("id")));
        model.addAttribute("profiles", page.getContent());
        model.addAttribute("page", page);
        return "welcome";
    }

    @GetMapping("/{uid}")
    public String profile(@PathVariable String uid, Model model) {
        Optional<Profile> profile = profileService.findByUid(uid);
        if (profile.isEmpty()) {
            return "error/profile-not-found";
        } else {
            model.addAttribute("profile", profile.get());
            return "profile";
        }

    }
}