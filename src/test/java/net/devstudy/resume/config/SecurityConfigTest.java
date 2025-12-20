package net.devstudy.resume.config;

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
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = SecurityConfigTest.TestController.class)
@Import({
        SecurityConfig.class,
        UiProperties.class,
        SecurityConfigTest.SecurityTestConfig.class,
        SecurityConfigTest.TestController.class
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void permitsPublicGetEndpoints() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/public"))
                .andExpect(status().isOk());
    }

    @Test
    void requiresAuthForProtectedEndpoints() throws Exception {
        mockMvc.perform(get("/me"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/account/test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/user/edit/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void requiresAuthForNonGetSingleSegment() throws Exception {
        mockMvc.perform(post("/public").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser
    void allowsAuthenticatedUserToAccessProtectedEndpoints() throws Exception {
        mockMvc.perform(get("/me"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/account/test"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/user/edit/profile"))
                .andExpect(status().isOk());
    }

    @TestConfiguration
    @SuppressWarnings("unused")
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

        @GetMapping("/")
        @SuppressWarnings("unused")
        String root() {
            return "root";
        }

        @GetMapping("/public")
        @SuppressWarnings("unused")
        String publicGet() {
            return "public";
        }

        @PostMapping("/public")
        @SuppressWarnings("unused")
        String publicPost() {
            return "public-post";
        }

        @GetMapping("/me")
        @SuppressWarnings("unused")
        String me() {
            return "me";
        }

        @GetMapping("/account/test")
        @SuppressWarnings("unused")
        String account() {
            return "account";
        }

        @GetMapping("/user/edit/profile")
        @SuppressWarnings("unused")
        String editProfile() {
            return "edit";
        }
    }
}
