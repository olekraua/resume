package net.devstudy.resume.profile.internal.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

class ProfileServiceImplTest {

    private ProfileServiceImpl service;

    @BeforeEach
    void setUp() {
        try {
            Constructor<?> constructor = ProfileServiceImpl.class.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            Object[] args = new Object[constructor.getParameterCount()];
            service = (ProfileServiceImpl) constructor.newInstance(args);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create ProfileServiceImpl for tests", ex);
        }
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
