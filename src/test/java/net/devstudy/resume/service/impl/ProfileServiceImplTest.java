package net.devstudy.resume.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import net.devstudy.resume.repository.storage.ProfileRepository;
import net.devstudy.resume.service.ProfileSearchService;

class ProfileServiceImplTest {

    private ProfileServiceImpl service;

    @BeforeEach
    void setUp() {
        ProfileRepository profileRepository = Mockito.mock(ProfileRepository.class);
        PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
        ProfileSearchService searchService = Mockito.mock(ProfileSearchService.class);
        service = new ProfileServiceImpl(profileRepository, null, null, null, null, null, null, null, passwordEncoder,
                searchService);
    }

    @Test
    void normalizeUid_basicSlugify() {
        assertEquals("john_doe", serviceTestAccessor().apply(" John_Doe "));
        assertEquals("oleksandr_kravchenko", serviceTestAccessor().apply("Oleksandr_Kravchenko"));
    }

    @Test
    void normalizeUid_spacesNotAllowed() {
        assertThrows(IllegalArgumentException.class, () -> serviceTestAccessor().apply("John Doe"));
    }

    @Test
    void normalizeUid_diacriticsNotAllowed() {
        assertThrows(IllegalArgumentException.class, () -> serviceTestAccessor().apply("café"));
    }

    @Test
    void normalizeUid_invalidTooShort() {
        assertThrows(IllegalArgumentException.class, () -> serviceTestAccessor().apply("a"));
    }

    private java.util.function.Function<String, String> serviceTestAccessor() {
        // доступ до protected-методу через анонімний клас-провайдер
        return s -> {
            try {
                var m = ProfileServiceImpl.class.getDeclaredMethod("normalizeUid", String.class);
                m.setAccessible(true);
                return (String) m.invoke(service, s);
            } catch (java.lang.reflect.InvocationTargetException ite) {
                if (ite.getCause() instanceof RuntimeException re) {
                    throw re;
                }
                throw new RuntimeException(ite.getCause() != null ? ite.getCause() : ite);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
