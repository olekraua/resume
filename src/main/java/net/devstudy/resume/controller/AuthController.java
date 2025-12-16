package net.devstudy.resume.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;

import net.devstudy.resume.security.CurrentProfileProvider;
import net.devstudy.resume.model.CurrentProfile;
import net.devstudy.resume.form.RegistrationForm;
import net.devstudy.resume.service.ProfileService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    private final ProfileService profileService;
    private final CurrentProfileProvider currentProfileProvider;

    public AuthController(ProfileService profileService, CurrentProfileProvider currentProfileProvider) {
        this.profileService = profileService;
        this.currentProfileProvider = currentProfileProvider;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        if (currentProfileProvider.getCurrentProfile() != null) {
            return "redirect:/me";
        }
        model.addAttribute("registrationForm", new RegistrationForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("registrationForm") @Valid RegistrationForm form,
            BindingResult bindingResult,
            Model model,
            HttpServletRequest request) {
        if (currentProfileProvider.getCurrentProfile() != null) {
            return "redirect:/me";
        }
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        try {
            var profile = profileService.register(form.getUid(), form.getFirstName(), form.getLastName(),
                    form.getPassword());
            CurrentProfile currentProfile = new CurrentProfile(profile);
            var authentication = new UsernamePasswordAuthenticationToken(currentProfile, null,
                    currentProfile.getAuthorities());
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
            return "redirect:/" + profile.getUid();
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/me")
    public String me() {
        CurrentProfile currentProfile = currentProfileProvider.getCurrentProfile();
        if (currentProfile == null) {
            return "redirect:/login";
        }
        return "redirect:/" + currentProfile.getUsername();
    }
}
