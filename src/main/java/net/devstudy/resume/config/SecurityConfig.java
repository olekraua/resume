package net.devstudy.resume.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                                        // публічні сторінки/статичні ресурси
                                        .requestMatchers("/", "/welcome", "/fragment/more", "/search",
                                                        "/error/**", "/css/**", "/favicon/**",
                                                        "/fonts/**", "/img/**",
                                                        "/js/**", "/media/**", "/uploads/**", "/favicon.ico",
                                                        "/login", "/register", "/register/**", "/restore/**")
                                        .permitAll()
                                        // профільні сторінки (GET /{uid}) публічні, але редагування/акаунт захищені
                                        .requestMatchers("/me", "/account/**", "/*/edit/**").authenticated()
                                        .requestMatchers(HttpMethod.GET, "/*").permitAll()
                                        .anyRequest().authenticated())
                        .userDetailsService(userDetailsService)
                        .formLogin(form -> form
                                        .loginPage("/login")
                                                .defaultSuccessUrl("/me", false)
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout")
                                                .permitAll());
                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
