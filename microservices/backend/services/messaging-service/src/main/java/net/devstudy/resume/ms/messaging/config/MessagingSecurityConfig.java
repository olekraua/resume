package net.devstudy.resume.ms.messaging.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import lombok.RequiredArgsConstructor;
import net.devstudy.resume.web.security.CurrentProfileJwtConverter;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.security.jwt.enabled", havingValue = "true")
public class MessagingSecurityConfig {

    private final CurrentProfileJwtConverter currentProfileJwtConverter;
    private final AccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain messagingSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth
                            .requestMatchers("/ws/**").permitAll()
                            .requestMatchers("/actuator/**").authenticated()
                            .requestMatchers("/api/**").authenticated();
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
