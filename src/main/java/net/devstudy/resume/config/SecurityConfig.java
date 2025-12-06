package net.devstudy.resume.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http, UserDetailsService userDetailsService)
                        throws Exception {
                http
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/", "/welcome", "/error/**", "/css/**", "/favicon/**",
                                                                "/fonts/**", "/img/**",
                                                                "/js/**", "/media/**", "/favicon.ico",
                                                                "/login", "/register")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .userDetailsService(userDetailsService)
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .defaultSuccessUrl("/me", true)
                                                .permitAll());
                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
