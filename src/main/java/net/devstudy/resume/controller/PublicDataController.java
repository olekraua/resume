package net.devstudy.resume.controller;

import net.devstudy.resume.annotation.constraints.FieldMatch;
import net.devstudy.resume.component.FormErrorConverter;
import net.devstudy.resume.domain.Profile;
import net.devstudy.resume.form.SignUpForm;
import net.devstudy.resume.model.CurrentProfile;
import net.devstudy.resume.service.FindProfileService;
import net.devstudy.resume.service.EditProfileService;
import net.devstudy.resume.util.SecurityUtil;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import static net.devstudy.resume.Constants.UI.MAX_PROFILES_PER_PAGE;

import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping
public class PublicDataController {

    private final FindProfileService findProfileService;
    private final FormErrorConverter formErrorConverter;
    private final EditProfileService editProfileService;

    public PublicDataController(FindProfileService findProfileService,
            EditProfileService editProfileService,
            FormErrorConverter formErrorConverter) {
        this.findProfileService = findProfileService;
        this.editProfileService = editProfileService;
        this.formErrorConverter = formErrorConverter;
    }

    // Головна -> welcome
    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/welcome";
    }

    // Профіль за UID
    @GetMapping("/{uid}")
    public String profile(@PathVariable String uid, Model model) {
        Profile profile = findProfileService.findByUid(uid);
        if (profile == null) {
            return "profile-not-found";
        }
        if (!profile.isCompleted()) {
            CurrentProfile currentProfile = SecurityUtil.getCurrentProfile();
            if (currentProfile == null || !currentProfile.getId().equals(profile.getId())) {
                return "profile-not-found";
            }
            return "redirect:/edit";
        }
        model.addAttribute("profile", profile);
        return "profile";
    }

    // Welcome + перша сторінка списку профілів
    @GetMapping("/welcome")
    public String listAll(Model model) {
        Page<Profile> profiles = findProfileService.findAll(
                PageRequest.of(0, MAX_PROFILES_PER_PAGE, Sort.by("id")));
        model.addAttribute("profiless", profiles.getContent());
        model.addAttribute("page", profiles);
        return "welcome";
    }

    // Пошук
    @GetMapping("/search")
    public String searchProfiles(@RequestParam(value = "query", required = false) String query,
            Model model,
            @PageableDefault(size = MAX_PROFILES_PER_PAGE) @SortDefault(sort = "id") Pageable pageable) {
        if (StringUtils.isBlank(query)) {
            return "redirect:/welcome";
        }
        Page<Profile> profiles = findProfileService.findBySearchQuery(query, pageable);
        model.addAttribute("profiles", profiles.getContent());
        model.addAttribute("page", profiles);
        model.addAttribute("query", java.net.URLDecoder.decode(query, StandardCharsets.UTF_8));
        return "search-results";
    }

    // Довантаження (AJAX-фрагмент)
    @GetMapping("/fragment/more")
    public String moreProfiles(Model model,
            @RequestParam(value = "query", required = false) String query,
            @PageableDefault(size = MAX_PROFILES_PER_PAGE) @SortDefault(sort = "id") Pageable pageable) {
        Page<Profile> profiles = StringUtils.isNotBlank(query)
                ? findProfileService.findBySearchQuery(query, pageable)
                : findProfileService.findAll(pageable);
        model.addAttribute("profiles", profiles.getContent());
        return "fragment/profile-items";
    }

    // Sign-in
    @GetMapping("/sign-in")
    public String signIn() {
        return "sign-in";
    }

    // Sign-up
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
        }
        Profile profile = editProfileService.createNewProfile(signUpForm);
        SecurityUtil.authentificateWithRememberMe(profile);
        return "redirect:/sign-up/success";
    }

    @GetMapping("/sign-up/success")
    public String signUpSuccess() {
        return "sign-up-success";
    }

    // Error view
    @GetMapping("/error")
    public String error() {
        return "error";
    }

    // Помилка логіну (повторний показ форми з повідомленням)
    @GetMapping("/sign-in-failed")
    public String signInFailed(HttpSession session) {
        return (session.getAttribute("SPRING_SECURITY_LAST_EXCEPTION") == null)
                ? "redirect:/sign-in"
                : "sign-in";
    }

    // Restore access
    @GetMapping("/restore")
    public String getRestoreAccess() {
        return "restore";
    }

    @GetMapping("/restore/success")
    public String getRestoreSuccess() {
        return "restore-success";
    }

    @PostMapping("/restore")
    public String processRestoreAccess(@RequestParam("uid") String anyUniqueId) {
        findProfileService.restoreAccess(anyUniqueId);
        return "redirect:/restore/success";
    }

    @GetMapping("/restore/{token}")
    public String restoreAccess(@PathVariable("token") String token) {
        Profile profile = findProfileService.findByRestoreToken(token);
        SecurityUtil.authenticate(profile);
        return "redirect:/edit/password";
    }
}
