package net.devstudy.resume.web.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import net.devstudy.resume.auth.api.dto.RestoreAccessForm;
import net.devstudy.resume.auth.api.dto.RestorePasswordForm;
import net.devstudy.resume.auth.api.service.RestoreAccessService;

@Controller
public class RestoreAccessController {

    private final RestoreAccessService restoreAccessService;
    private final boolean showRestoreLink;

    public RestoreAccessController(RestoreAccessService restoreAccessService,
            @Value("${app.restore.show-link:false}") boolean showRestoreLink) {
        this.restoreAccessService = restoreAccessService;
        this.showRestoreLink = showRestoreLink;
    }

    @GetMapping("/restore")
    public String restoreForm(Model model) {
        if (!model.containsAttribute("restoreAccessForm")) {
            model.addAttribute("restoreAccessForm", new RestoreAccessForm());
        }
        return "auth/restore";
    }

    @PostMapping("/restore")
    public String restore(@ModelAttribute("restoreAccessForm") @Valid RestoreAccessForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/restore";
        }
        String appHost = ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString();
        try {
            String link = restoreAccessService.requestRestore(form.getIdentifier(), appHost);
            redirectAttributes.addFlashAttribute("restoreLink", link);
        } catch (IllegalArgumentException ex) {
            // Intentionally ignore to avoid account enumeration.
        }
        return "redirect:/restore/success";
    }

    @GetMapping("/restore/success")
    public String restoreSuccess(Model model) {
        model.addAttribute("showRestoreLink", showRestoreLink);
        return "auth/restore-success";
    }

    @GetMapping("/restore/{token}")
    public String restorePasswordForm(@PathVariable String token, Model model) {
        if (restoreAccessService.findProfileByToken(token).isEmpty()) {
            return "redirect:/restore?invalid";
        }
        model.addAttribute("token", token);
        model.addAttribute("restorePasswordForm", new RestorePasswordForm());
        return "auth/restore-password";
    }

    @PostMapping("/restore/{token}")
    public String restorePassword(@PathVariable String token,
            @ModelAttribute("restorePasswordForm") @Valid RestorePasswordForm form,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("token", token);
            return "auth/restore-password";
        }
        try {
            restoreAccessService.resetPassword(token, form.getPassword());
            return "redirect:/login?restored";
        } catch (IllegalArgumentException ex) {
            return "redirect:/restore?invalid";
        }
    }
}
