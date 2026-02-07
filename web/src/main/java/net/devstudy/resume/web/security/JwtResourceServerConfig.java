package net.devstudy.resume.web.security;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@ConditionalOnExpression("${app.security.jwt.enabled:false} && !${app.security.oidc.enabled:false}")
public class JwtResourceServerConfig {

    private final CurrentProfileJwtConverter currentProfileJwtConverter;
    private final AccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain jwtFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth
                            .requestMatchers("/api/auth/**", "/api/csrf").permitAll()
                            .requestMatchers("/internal/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                            .requestMatchers(HttpMethod.GET,
                                    "/api/me",
                                    "/api/profiles",
                                    "/api/profiles/*",
                                    "/api/search",
                                    "/api/suggest",
                                    "/api/static-data")
                            .permitAll()
                            .requestMatchers("/api/**").authenticated()
                            .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher("/actuator/**"))
                            .authenticated();
                    auth.anyRequest().denyAll();
                })
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(currentProfileJwtConverter)))
                .exceptionHandling(ex -> {
                    ex.accessDeniedHandler(accessDeniedHandler);
                    ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
                });

        return http.build();
    }
}
