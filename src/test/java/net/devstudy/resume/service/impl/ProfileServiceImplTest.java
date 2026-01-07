package net.devstudy.resume.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import net.devstudy.resume.component.CertificateFileStorage;
import net.devstudy.resume.component.impl.UploadCertificateLinkTempStorage;
import net.devstudy.resume.repository.storage.ProfileRepository;
import net.devstudy.resume.security.CurrentProfileProvider;
import net.devstudy.resume.service.ProfileSearchService;

class ProfileServiceImplTest {

    private ProfileServiceImpl service;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        ProfileRepository profileRepository = Mockito.mock(ProfileRepository.class);
        CertificateFileStorage certificateFileStorage = Mockito.mock(CertificateFileStorage.class);
        UploadCertificateLinkTempStorage uploadCertificateLinkTempStorage =
                Mockito.mock(UploadCertificateLinkTempStorage.class);
        PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
        ProfileSearchService searchService = Mockito.mock(ProfileSearchService.class);
        CurrentProfileProvider currentProfileProvider = Mockito.mock(CurrentProfileProvider.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        service = new ProfileServiceImpl(profileRepository, null, null, null, null, null, null, null,
                certificateFileStorage, uploadCertificateLinkTempStorage, passwordEncoder, searchService,
                currentProfileProvider, eventPublisher);
    }

    @Test
    void normalizeUidBasicSlugify() {
        assertEquals("john_doe", serviceTestAccessor().apply(" John_Doe "));
        assertEquals("oleksandr_kravchenko", serviceTestAccessor().apply("Oleksandr_Kravchenko"));
    }

    @Test
    void normalizeUidSpacesNotAllowed() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> serviceTestAccessor().apply("John Doe"));
        org.junit.jupiter.api.Assertions.assertNotNull(ex);
    }

    @Test
    void normalizeUidDiacriticsNotAllowed() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> serviceTestAccessor().apply("café"));
        org.junit.jupiter.api.Assertions.assertNotNull(ex);
    }

    @Test
    void normalizeUidInvalidTooShort() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> serviceTestAccessor().apply("a"));
        org.junit.jupiter.api.Assertions.assertNotNull(ex);
    }

    private java.util.function.Function<String, String> serviceTestAccessor() {
        // доступ до protected-методу через анонімний клас-провайдер
        return s -> {
            try {
                var m = ProfileServiceImpl.class.getDeclaredMethod("normalizeUid", String.class);
                m.setAccessible(true);
                return (String) m.invoke(service, s);
            } catch (java.lang.reflect.InvocationTargetException ite) {
                Throwable cause = ite.getCause();
                if (cause instanceof RuntimeException re) {
                    throw re;
                }
                throw new RuntimeException(cause != null ? cause : ite);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
