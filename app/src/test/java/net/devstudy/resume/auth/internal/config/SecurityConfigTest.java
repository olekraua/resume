package net.devstudy.resume.auth.internal.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = SecurityConfigTest.TestController.class)
@Import({
        SecurityConfig.class,
        SecurityConfigTest.SecurityTestConfig.class,
        SecurityConfigTest.TestController.class
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void permitsPublicApiEndpoints() throws Exception {
        mockMvc.perform(get("/api/csrf"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/static-data"))
                .andExpect(status().isOk());
    }

    @Test
    void requiresAuthForProtectedApiEndpoints() throws Exception {
        mockMvc.perform(get("/api/secure"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void allowsAuthenticatedUserToAccessProtectedApiEndpoints() throws Exception {
        mockMvc.perform(get("/api/secure"))
                .andExpect(status().isOk());
    }

    @Test
    void providesPasswordEncoderBean() {
        assertInstanceOf(BCryptPasswordEncoder.class, passwordEncoder);
    }

    @TestConfiguration
    static class SecurityTestConfig {

        @Bean
        UserDetailsService userDetailsService() {
            return new InMemoryUserDetailsManager(User.withUsername("user")
                    .password("{noop}password")
                    .roles("USER")
                    .build());
        }

        @Bean
        AccessDeniedHandler accessDeniedHandler() {
            return (request, response, ex) -> response.sendError(403);
        }

    }

    @RestController
    public static class TestController {

        @GetMapping("/api/csrf")
        String csrf() {
            return "csrf";
        }

        @GetMapping("/api/static-data")
        String staticData() {
            return "static-data";
        }

        @GetMapping("/api/secure")
        String secure() {
            return "secure";
        }
    }
}
