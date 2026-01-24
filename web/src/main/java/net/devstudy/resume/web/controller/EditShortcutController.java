package net.devstudy.resume.web.controller;

import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import net.devstudy.resume.auth.api.model.CurrentProfile;
import net.devstudy.resume.auth.api.security.CurrentProfileProvider;

@Controller
@RequestMapping("/edit")
public class EditShortcutController {

    private static final Set<String> SECTIONS = Set.of(
            "profile",
            "contacts",
            "skills",
            "practics",
            "certificates",
            "education",
            "courses",
            "languages",
            "hobbies",
            "info",
            "photo",
            "password");

    private final CurrentProfileProvider currentProfileProvider;

    public EditShortcutController(CurrentProfileProvider currentProfileProvider) {
        this.currentProfileProvider = currentProfileProvider;
    }

    @GetMapping
    public String editRoot() {
        CurrentProfile current = currentProfileProvider.getCurrentProfile();
        if (current == null) {
            return "redirect:/login";
        }
        return "redirect:/" + current.getUsername() + "/edit/profile";
    }

    @GetMapping("/{section}")
    public String editSection(@PathVariable String section) {
        CurrentProfile current = currentProfileProvider.getCurrentProfile();
        if (current == null) {
            return "redirect:/login";
        }
        String target = SECTIONS.contains(section) ? section : "profile";
        return "redirect:/" + current.getUsername() + "/edit/" + target;
    }
}
