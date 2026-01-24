package net.devstudy.resume.profile.api.model;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.when;

import net.devstudy.resume.staticdata.api.model.SkillCategory;
import net.devstudy.resume.auth.api.model.CurrentProfile;
import net.devstudy.resume.profile.internal.repository.storage.ProfileRepository;
import net.devstudy.resume.staticdata.api.service.StaticDataService;
import net.devstudy.resume.testcontainers.PostgresIntegrationTest;

@AutoConfigureMockMvc
@Import(EditProfileAccessIntegrationTest.SecurityTestConfig.class)
@Tag("integration")
class EditProfileAccessIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProfileRepository profileRepository;

    @MockitoBean
    private StaticDataService staticDataService;

    @Test
    void returnsForbiddenWhenAuthenticatedUserEditsAnotherProfile() throws Exception {
        Profile owner = createProfile("owner-user");
        Profile other = createProfile("other-user");
        CurrentProfile currentProfile = new CurrentProfile(owner);
        SkillCategory category = new SkillCategory();
        category.setCategory("Programming");
        when(staticDataService.findSkillCategories()).thenReturn(java.util.List.of(category));

        mockMvc.perform(get("/" + other.getUid() + "/edit/profile")
                .with(user(currentProfile)))
                .andExpect(status().isForbidden());
    }

    private Profile createProfile(String uid) {
        Profile profile = new Profile();
        profile.setUid(uid);
        profile.setFirstName("Test");
        profile.setLastName("User");
        profile.setPassword("password");
        profile.setCompleted(false);
        return profileRepository.save(profile);
    }

    @TestConfiguration
    static class SecurityTestConfig {

        @Bean
        AccessDeniedHandler accessDeniedHandler() {
            return (request, response, ex) -> response.sendError(403);
        }
    }
}
