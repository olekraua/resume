package net.devstudy.resume.web.controller.api;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import lombok.RequiredArgsConstructor;
import net.devstudy.resume.auth.api.dto.RegistrationForm;
import net.devstudy.resume.auth.api.dto.RestoreAccessForm;
import net.devstudy.resume.auth.api.dto.RestorePasswordForm;
import net.devstudy.resume.auth.api.model.CurrentProfile;
import net.devstudy.resume.auth.api.security.CurrentProfileProvider;
import net.devstudy.resume.auth.api.service.ProfileAccountService;
import net.devstudy.resume.auth.api.service.RestoreAccessService;
import net.devstudy.resume.auth.api.service.UidSuggestionService;
import net.devstudy.resume.profile.api.exception.UidAlreadyExistsException;
import net.devstudy.resume.profile.api.dto.internal.ProfileAuthResponse;
import net.devstudy.resume.profile.api.dto.internal.ProfileRegistrationRequest;
import net.devstudy.resume.shared.component.DataBuilder;
import net.devstudy.resume.shared.dto.ApiErrorResponse;
import net.devstudy.resume.web.controller.SessionApiController;
import net.devstudy.resume.web.security.RememberMeSupport;
import net.devstudy.resume.web.api.ApiErrorUtils;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final ProfileAccountService profileAccountService;
    private final CurrentProfileProvider currentProfileProvider;
    private final UidSuggestionService uidSuggestionService;
    private final RestoreAccessService restoreAccessService;
    private final DataBuilder dataBuilder;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final RememberMeSupport rememberMeSupport;

    @Value("${app.restore.show-link:false}")
    private boolean showRestoreLink;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, BindingResult bindingResult,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        if (bindingResult.hasErrors()) {
            return ApiErrorUtils.badRequest(bindingResult, httpRequest);
        }
        CurrentProfile existing = currentProfileProvider.getCurrentProfile();
        if (existing != null) {
            return ResponseEntity.ok(toSessionResponse(existing));
        }
        AuthenticationManager authenticationManager;
        try {
            authenticationManager = authenticationConfiguration.getAuthenticationManager();
        } catch (Exception ex) {
            return ApiErrorUtils.error(HttpStatus.INTERNAL_SERVER_ERROR, "Authentication unavailable", httpRequest);
        }
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (AuthenticationException ex) {
            return ApiErrorUtils.error(HttpStatus.UNAUTHORIZED, "Invalid username or password", httpRequest);
        }
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        rememberMeSupport.loginSuccess(httpRequest, httpResponse, authentication, request.rememberMe());
        CurrentProfile currentProfile = resolveCurrentProfile(authentication);
        return ResponseEntity.ok(toSessionResponse(currentProfile));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        rememberMeSupport.logout(request, response, authentication);
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegistrationForm form, BindingResult bindingResult,
            HttpServletRequest request, HttpServletResponse response) {
        if (currentProfileProvider.getCurrentProfile() != null) {
            return ApiErrorUtils.error(HttpStatus.CONFLICT, "Already authenticated", request);
        }
        if (bindingResult.hasErrors()) {
            return ApiErrorUtils.badRequest(bindingResult, request);
        }
        try {
            ProfileAuthResponse profile = profileAccountService.register(
                    new ProfileRegistrationRequest(form.getUid(), form.getFirstName(), form.getLastName(),
                            form.getPassword()));
            CurrentProfile currentProfile = new CurrentProfile(profile);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    currentProfile, null, currentProfile.getAuthorities());
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
            rememberMeSupport.loginSuccess(request, response, authentication, false);
            return ResponseEntity.status(HttpStatus.CREATED).body(toSessionResponse(currentProfile));
        } catch (UidAlreadyExistsException ex) {
            List<String> suggestions = uidSuggestionService.suggest(ex.getUid());
            ApiErrorResponse error = ApiErrorResponse.of(
                    HttpStatus.CONFLICT,
                    ex.getMessage(),
                    ApiErrorUtils.resolvePath(request),
                    List.of(new ApiErrorResponse.FieldError("uid", ex.getMessage()))
            );
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new UidConflictResponse(error, suggestions));
        } catch (IllegalArgumentException ex) {
            return ApiErrorUtils.error(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
        }
    }

    @GetMapping("/uid-hint")
    public UidHintResponse uidHint(@RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName) {
        return new UidHintResponse(dataBuilder.buildProfileUid(firstName, lastName));
    }

    @PostMapping("/restore")
    public ResponseEntity<?> requestRestore(@Valid @RequestBody RestoreAccessForm form, BindingResult bindingResult,
            HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return ApiErrorUtils.badRequest(bindingResult, request);
        }
        String appHost = ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString();
        String link = null;
        try {
            link = restoreAccessService.requestRestore(form.getIdentifier(), appHost);
        } catch (IllegalArgumentException ex) {
            link = null;
        }
        if (!showRestoreLink) {
            link = null;
        }
        return ResponseEntity.ok(new RestoreRequestResponse(true, link));
    }

    @GetMapping("/restore/{token}")
    public ResponseEntity<?> restoreStatus(@PathVariable String token, HttpServletRequest request) {
        boolean valid = restoreAccessService.findProfileByToken(token).isPresent();
        if (!valid) {
            return ApiErrorUtils.error(HttpStatus.NOT_FOUND, "Restore token invalid", request);
        }
        return ResponseEntity.ok(new RestoreTokenResponse(true));
    }

    @PostMapping("/restore/{token}")
    public ResponseEntity<?> restorePassword(@PathVariable String token,
            @Valid @RequestBody RestorePasswordForm form,
            BindingResult bindingResult,
            HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return ApiErrorUtils.badRequest(bindingResult, request);
        }
        try {
            restoreAccessService.resetPassword(token, form.getPassword());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ApiErrorUtils.error(HttpStatus.BAD_REQUEST, "Restore token invalid", request);
        }
    }

    private SessionApiController.SessionResponse toSessionResponse(CurrentProfile currentProfile) {
        if (currentProfile == null) {
            return new SessionApiController.SessionResponse(false, null, null);
        }
        return new SessionApiController.SessionResponse(
                true,
                currentProfile.getUsername(),
                currentProfile.getFullName()
        );
    }

    private CurrentProfile resolveCurrentProfile(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CurrentProfile currentProfile) {
            return currentProfile;
        }
        return currentProfileProvider.getCurrentProfile();
    }

    public record LoginRequest(
            @jakarta.validation.constraints.NotBlank String username,
            @jakarta.validation.constraints.NotBlank String password,
            boolean rememberMe
    ) {
    }

    public record UidHintResponse(String uid) {
    }

    public record RestoreRequestResponse(boolean requested, String restoreLink) {
    }

    public record RestoreTokenResponse(boolean valid) {
    }

    public record UidConflictResponse(ApiErrorResponse error, List<String> uidSuggestions) {
    }
}
