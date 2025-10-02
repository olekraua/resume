package net.devstudy.resume.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Service;

@Service
public class RememberMeService extends PersistentTokenBasedRememberMeServices {

    public RememberMeService(UserDetailsService userDetailsService,
                             PersistentTokenRepository tokenRepository) {
        super("resume-online", userDetailsService, tokenRepository);
        // за потреби: setTokenValiditySeconds(...), setAlwaysRemember(true), setCookieName(...)
    }

    public void createAutoLoginToken(HttpServletRequest request,
                                     HttpServletResponse response,
                                     Authentication successfulAuthentication) {
        super.onLoginSuccess(request, response, successfulAuthentication);
    }
}

