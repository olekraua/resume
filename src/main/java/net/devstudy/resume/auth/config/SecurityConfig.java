package net.devstudy.resume.auth.config;

import java.time.Duration;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.util.StringUtils;

import net.devstudy.resume.auth.service.impl.RememberMeService;

@Configuration
public class SecurityConfig {

    private static final String DEFAULT_REMEMBER_ME_KEY = "resume-remember-me-key";
    private static final String DEFAULT_REMEMBER_ME_PARAMETER = "remember-me";
    private static final String DEFAULT_REMEMBER_ME_COOKIE_NAME = "remember-me";
    private static final Duration DEFAULT_REMEMBER_ME_TTL = Duration.ofDays(14);

    @Value("${app.security.remember-me.key:resume-remember-me-key}")
    private String rememberMeKey;

    @Value("${app.security.remember-me.token-ttl:PT336H}")
    private Duration rememberMeTtl;

    @Value("${app.security.remember-me.parameter:remember-me}")
    private String rememberMeParameter;

    @Value("${app.security.remember-me.cookie-name:remember-me}")
    private String rememberMeCookieName;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
            UserDetailsService userDetailsService,
            AccessDeniedHandler accessDeniedHandler,
            ObjectProvider<RememberMeService> rememberMeServiceProvider)
            throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // публічні сторінки/статичні ресурси
                        .requestMatchers("/", "/welcome", "/fragment/more", "/search",
                                "/error/**", "/css/**", "/favicon/**",
                                "/fonts/**", "/img/**",
                                "/js/**", "/media/**", "/uploads/**", "/favicon.ico")
                        .permitAll()
                        .requestMatchers("/login", "/register", "/register/**", "/restore/**")
                        .anonymous()
                        // профільні сторінки (GET /{uid}) публічні, але редагування/акаунт захищені
                        .requestMatchers("/me", "/account/**", "/*/edit/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/*").permitAll()
                        .anyRequest().authenticated())
                .userDetailsService(userDetailsService)
                .exceptionHandling(ex -> ex.accessDeniedHandler(accessDeniedHandler))
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/me", false));

        RememberMeService rememberMeService = rememberMeServiceProvider.getIfAvailable();
        if (rememberMeService != null) {
            http.rememberMe(remember -> remember
                    .tokenRepository(rememberMeService)
                    .key(normalizeRememberMeValue(rememberMeKey, DEFAULT_REMEMBER_ME_KEY))
                    .rememberMeParameter(normalizeRememberMeValue(rememberMeParameter,
                            DEFAULT_REMEMBER_ME_PARAMETER))
                    .rememberMeCookieName(normalizeRememberMeValue(rememberMeCookieName,
                            DEFAULT_REMEMBER_ME_COOKIE_NAME))
                    .tokenValiditySeconds(toRememberMeSeconds(rememberMeTtl))
                    .userDetailsService(userDetailsService));
        }

        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private int toRememberMeSeconds(Duration ttl) {
        Duration normalized = ttl;
        if (normalized == null || normalized.isNegative() || normalized.isZero()) {
            normalized = DEFAULT_REMEMBER_ME_TTL;
        }
        long seconds = normalized.getSeconds();
        if (seconds > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) seconds;
    }

    private String normalizeRememberMeValue(String value, String fallback) {
        if (StringUtils.hasText(value)) {
            return value.trim();
        }
        return fallback;
    }
}
