package net.devstudy.resume.controller;

import static net.devstudy.resume.Constants.UI.MAX_PROFILES_PER_PAGE;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import net.devstudy.resume.annotation.constraints.FieldMatch;
import net.devstudy.resume.component.FormErrorConverter;
import net.devstudy.resume.domain.Profile;
import net.devstudy.resume.form.SignUpForm;
import net.devstudy.resume.model.CurrentProfile;
import net.devstudy.resume.service.EditProfileService;
import net.devstudy.resume.service.FindProfileService;
import net.devstudy.resume.util.SecurityUtil;

@Controller
public class PublicDataController {

    @Autowired private FindProfileService findProfileService;
    @Autowired private EditProfileService editProfileService;
    @Autowired private FormErrorConverter formErrorConverter;

    @GetMapping("/{uid}")
    public String profile(@PathVariable String uid, Model model) {
        Profile profile = findProfileService.findByUid(uid);
        if (profile == null) {
            return "profile-not-found";
        } else if (!profile.isCompleted()) {
            CurrentProfile currentProfile = SecurityUtil.getCurrentProfile();
            if (currentProfile == null || !currentProfile.getId().equals(profile.getId())) {
                return "profile-not-found";
            } else {
                return "redirect:/edit";
            }
        } else {
            model.addAttribute("profile", profile);
            return "profile";
        }
    }

    @GetMapping("/welcome")
    public String listAll(Model model) {
        Page<Profile> profiles =
            findProfileService.findAll(PageRequest.of(0, MAX_PROFILES_PER_PAGE, Sort.by("id")));
        model.addAttribute("profiles", profiles.getContent());
        model.addAttribute("page", profiles);
        return "welcome";
    }

    @GetMapping("/search")
    public String searchProfiles(@RequestParam(value = "query", required = false) String query,
                                 Model model,
                                 @PageableDefault(size = MAX_PROFILES_PER_PAGE)
                                 @SortDefault(sort = "id") Pageable pageable) {
        if (query == null || query.isBlank()) {
            return "redirect:/welcome";
        } else {
            Page<Profile> profiles = findProfileService.findBySearchQuery(query, pageable);
            model.addAttribute("profiles", profiles.getContent());
            model.addAttribute("page", profiles);
            model.addAttribute("query", URLDecoder.decode(query, StandardCharsets.UTF_8));
            return "search-results";
        }
    }

    @GetMapping("/fragment/more")
    public String moreProfiles(Model model,
                               @RequestParam(value = "query", required = false) String query,
                               @PageableDefault(size = MAX_PROFILES_PER_PAGE)
                               @SortDefault(sort = "id") Pageable pageable) {
        Page<Profile> profiles = (query != null && !query.isBlank())
                ? findProfileService.findBySearchQuery(query, pageable)
                : findProfileService.findAll(pageable);
        model.addAttribute("profiles", profiles.getContent());
        return "fragment/profile-items";
    }

    @GetMapping("/sign-in")
    public String signIn() { return "sign-in"; }

    @GetMapping("/sign-up")
    public String signUp(Model model) {
        model.addAttribute("profileForm", new SignUpForm());
        return "sign-up";
    }

    @PostMapping("/sign-up")
    public String signUp(@Valid @ModelAttribute("profileForm") SignUpForm signUpForm,
                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            formErrorConverter.convertToFieldError(FieldMatch.class, signUpForm, bindingResult);
            return "sign-up";
        } else {
            Profile profile = editProfileService.createNewProfile(signUpForm);
            SecurityUtil.authentificateWithRememberMe(profile);
            return "redirect:/sign-up/success";
        }
    }

    @GetMapping("/sign-up/success")
    public String signUpSuccess() { return "sign-up-success"; }

    @GetMapping("/error")
    public String error() { return "error"; }

    @GetMapping("/sign-in-failed")
    public String signInFailed(HttpSession session) {
        return (session.getAttribute("SPRING_SECURITY_LAST_EXCEPTION") == null)
               ? "redirect:/sign-in" : "sign-in";
    }

    @GetMapping("/restore")
    public String getRestoreAccess() { return "restore"; }

    @PostMapping("/restore")
    public String processRestoreAccess(@RequestParam("uid") String anyUniqueId) {
        findProfileService.restoreAccess(anyUniqueId);
        return "redirect:/restore/success";
    }

    @GetMapping("/restore/success")
    public String getRestoreSuccess() { return "restore-success"; }

    @GetMapping("/restore/{token}")
    public String restoreAccess(@PathVariable("token") String token) {
        Profile profile = findProfileService.findByRestoreToken(token);
        SecurityUtil.authentificate(profile);
        return "redirect:/edit/password";
    }
}
