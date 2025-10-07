package net.devstudy.resume.configuration;

import static org.springframework.security.config.Customizer.withDefaults;

import net.devstudy.resume.Constants;
import net.devstudy.resume.repository.storage.MongoPersistentTokenRepositoryAdapter;
import net.devstudy.resume.repository.storage.RememberMeTokenRepository;
import net.devstudy.resume.service.impl.RememberMeService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        // Beans приходять з контексту (як і раніше @Autowired, але через
        // конструктор/параметри бінів)
        private final UserDetailsService userDetailsService;
        private final RememberMeTokenRepository rememberMeTokenRepository;
        private final RememberMeService persistentTokenRememberMeService;
        private final AccessDeniedHandler accessDeniedHandler;

        public SecurityConfig(UserDetailsService userDetailsService,
                        RememberMeTokenRepository rememberMeTokenRepository,
                        RememberMeService persistentTokenRememberMeService,
                        AccessDeniedHandler accessDeniedHandler) {
                this.userDetailsService = userDetailsService;
                this.rememberMeTokenRepository = rememberMeTokenRepository;
                this.persistentTokenRememberMeService = persistentTokenRememberMeService;
                this.accessDeniedHandler = accessDeniedHandler;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                // Авторизація запитів (antMatchers -> requestMatchers)
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/my-profile", "/edit", "/edit/**", "/remove")
                                                .hasAuthority(Constants.USER)
                                                .anyRequest().permitAll())

                                // Form login
                                .formLogin(form -> form
                                                .loginPage("/sign-in")
                                                .loginProcessingUrl("/sign-in-handler")
                                                .usernameParameter("uid")
                                                .passwordParameter("password")
                                                .defaultSuccessUrl("/my-profile", true)
                                                .failureUrl("/sign-in-failed"))

                                // Logout
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/welcome")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID"))

                                // Remember-me (використовуєш свій RememberMeService з persistent tokens)
                                .rememberMe(rm -> rm
                                                .rememberMeParameter("remember-me")
                                                .rememberMeServices(persistentTokenRememberMeService))

                                // 403 Access Denied
                                .exceptionHandling(ex -> ex.accessDeniedHandler(accessDeniedHandler))

                                // Підключаємо наш DAO provider (щоб точно спрацював PasswordEncoder)
                                .authenticationProvider(daoAuthenticationProvider())

                                // Інше залишаємо за замовчуванням
                                .csrf(withDefaults());

                return http.build();
        }

        @Bean
        public PersistentTokenRepository persistentTokenRepository() {
                return new MongoPersistentTokenRepositoryAdapter(rememberMeTokenRepository);
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        /**
         * DaoAuthenticationProvider + наш UserDetailsService + BCrypt
         * (аналог твоєї configure(AuthenticationManagerBuilder ...))
         */
        @Bean
        public DaoAuthenticationProvider daoAuthenticationProvider() {
                DaoAuthenticationProvider p = new DaoAuthenticationProvider();
                p.setUserDetailsService(userDetailsService);
                p.setPasswordEncoder(passwordEncoder());
                return p;
        }

        /**
         * Якщо тобі потрібен сам AuthenticationManager як бін:
         * (опційно; у більшості випадків достатньо authenticationProvider(..) вище)
         */
        @Bean
        public AuthenticationManager authenticationManager() {
                return authentication -> daoAuthenticationProvider().authenticate(authentication);
        }
}