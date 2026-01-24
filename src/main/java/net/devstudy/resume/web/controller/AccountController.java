package net.devstudy.resume.web.controller;

import lombok.RequiredArgsConstructor;
import net.devstudy.resume.profile.api.model.Profile;
import net.devstudy.resume.auth.api.dto.ChangePasswordForm;
import net.devstudy.resume.auth.api.dto.ChangeLoginForm;
import net.devstudy.resume.auth.api.security.CurrentProfileProvider;
import net.devstudy.resume.profile.api.service.ProfileService;
import net.devstudy.resume.auth.api.service.UidSuggestionService;
import net.devstudy.resume.profile.api.exception.UidAlreadyExistsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Optional;

@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final ProfileService profileService;
    private final PasswordEncoder passwordEncoder;
    private final CurrentProfileProvider currentProfileProvider;
    private final UidSuggestionService uidSuggestionService;

    @GetMapping("/password")
    public String passwordForm(Model model) {
        model.addAttribute("changePasswordForm", new ChangePasswordForm());
        return "auth/change-password";
    }

    @PostMapping("/password")
    public String changePassword(@Valid ChangePasswordForm form, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/change-password";
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            model.addAttribute("errorMessage", "Новий пароль і підтвердження не збігаються");
            return "auth/change-password";
        }
        Long currentId = currentProfileProvider.getCurrentId();
        if (currentId == null) {
            return "redirect:/login";
        }
        Optional<Profile> profileOpt = profileService.findById(currentId);
        if (profileOpt.isEmpty()
                || !passwordEncoder.matches(form.getCurrentPassword(), profileOpt.get().getPassword())) {
            model.addAttribute("errorMessage", "Невірний поточний пароль");
            return "auth/change-password";
        }
        profileService.updatePassword(currentId, form.getNewPassword());
        return "redirect:/account/password?success";
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        Long currentId = currentProfileProvider.getCurrentId();
        if (currentId == null) {
            return "redirect:/login";
        }
        Optional<Profile> profileOpt = profileService.findById(currentId);
        if (profileOpt.isEmpty()) {
            return "redirect:/login";
        }
        ChangeLoginForm form = new ChangeLoginForm();
        form.setNewUid(profileOpt.get().getUid());
        model.addAttribute("currentUid", profileOpt.get().getUid());
        model.addAttribute("changeLoginForm", form);
        return "auth/change-login";
    }

    @PostMapping("/login")
    public String changeLogin(@Valid ChangeLoginForm form, BindingResult bindingResult, Model model) {
        Long currentId = currentProfileProvider.getCurrentId();
        if (currentId == null) {
            return "redirect:/login";
        }
        Optional<Profile> profileOpt = profileService.findById(currentId);
        if (profileOpt.isEmpty()) {
            return "redirect:/login";
        }

        Profile profile = profileOpt.get();

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUid", profile.getUid());
            return "auth/change-login";
        }
        try {
            profileService.updateUid(currentId, form.getNewUid());
        } catch (UidAlreadyExistsException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("uidSuggestions", uidSuggestionService.suggest(ex.getUid()));
            model.addAttribute("currentUid", profile.getUid());
            model.addAttribute("uidError", ex.getMessage());
            return "auth/change-login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("currentUid", profile.getUid());
            model.addAttribute("uidError", ex.getMessage());
            return "auth/change-login";
        }
        return "redirect:/login?loginChanged";
    }

    @GetMapping("/remove")
    public String removeAccountForm(Model model) {
        var currentProfile = currentProfileProvider.getCurrentProfile();
        if (currentProfile == null) {
            return "redirect:/login";
        }
        model.addAttribute("currentProfile", currentProfile);
        return "auth/remove";
    }

    @PostMapping("/remove")
    public String removeAccount(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) {
        Long currentId = currentProfileProvider.getCurrentId();
        if (currentId == null) {
            return "redirect:/login";
        }
        profileService.removeProfile(currentId);
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return "redirect:/welcome?removed";
    }
}
