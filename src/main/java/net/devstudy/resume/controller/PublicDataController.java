package net.devstudy.resume.controller;

import net.devstudy.resume.entity.Profile;
import net.devstudy.resume.form.ChangeLoginForm;
import net.devstudy.resume.model.CurrentProfile;
import net.devstudy.resume.security.CurrentProfileProvider;
import net.devstudy.resume.service.ProfileService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import lombok.RequiredArgsConstructor;

import static net.devstudy.resume.shared.constants.Constants.UI.MAX_PROFILES_PER_PAGE;

import java.util.Optional;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class PublicDataController {
    private final ProfileService profileService;
    private final CurrentProfileProvider currentProfileProvider;

    

    @GetMapping("/")
    public String redirectToWelcome() {
        return "redirect:/welcome";
    }

    @GetMapping("/welcome")
    public String listAll(Model model, @RequestParam(value = "page", defaultValue = "0") int pageNumber,
            @RequestParam(value = "query", required = false) String query) {
        String q = query == null ? "" : query.trim();
        Page<Profile> page = q.isEmpty()
                ? profileService.findAll(PageRequest.of(pageNumber, MAX_PROFILES_PER_PAGE, Sort.by("id")))
                : profileService.search(q, PageRequest.of(pageNumber, MAX_PROFILES_PER_PAGE, Sort.by("id")));
        model.addAttribute("profiles", page.getContent());
        model.addAttribute("page", page);
        model.addAttribute("query", q);
        return "welcome";
    }

    @GetMapping("/fragment/more")
    public String loadMoreProfiles(@RequestParam("page") int pageNumber,
            @RequestParam(value = "query", required = false) String query, Model model) {
        String q = query == null ? "" : query.trim();
        Page<Profile> page = q.isEmpty()
                ? profileService.findAll(PageRequest.of(pageNumber, MAX_PROFILES_PER_PAGE, Sort.by("id")))
                : profileService.search(q, PageRequest.of(pageNumber, MAX_PROFILES_PER_PAGE, Sort.by("id")));
        model.addAttribute("profiles", page.getContent());
        model.addAttribute("query", q);
        return "profiles :: items";
    }

    @GetMapping("/{uid}")
    public String profile(@PathVariable String uid, Model model) {
        Optional<Profile> profileOptional = profileService.findWithAllByUid(uid);
        if (profileOptional.isEmpty()) {
            return "error/profile-not-found";
        }

        Profile profile = profileOptional.get();

        CurrentProfile currentProfile = currentProfileProvider.getCurrentProfile();
        boolean ownProfile = currentProfile != null && currentProfile.getId().equals(profile.getId());
        model.addAttribute("ownProfile", ownProfile);
        if (ownProfile) {
            ChangeLoginForm changeLoginForm = new ChangeLoginForm();
            changeLoginForm.setNewUid(profile.getUid());
            model.addAttribute("changeLoginForm", changeLoginForm);
            model.addAttribute("currentUid", profile.getUid());
        }

        model.addAttribute("profile", profile);
        return "profile";
    }

    @GetMapping("/search")
    public String search(@RequestParam("q") String query,
            @RequestParam(value = "page", defaultValue = "0") int pageNumber,
            Model model) {
        if (query == null || query.trim().isEmpty()) {
            return "redirect:/welcome";
        }
        Page<Profile> page = profileService.search(query.trim(),
                PageRequest.of(pageNumber, MAX_PROFILES_PER_PAGE, Sort.by("id")));
        model.addAttribute("profiles", page.getContent());
        model.addAttribute("page", page);
        model.addAttribute("query", query.trim());
        return "search-results";
    }
}
