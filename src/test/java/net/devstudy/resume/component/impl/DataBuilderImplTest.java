package net.devstudy.resume.component.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.devstudy.resume.component.TranslitConverter;

class DataBuilderImplTest {

    private DataBuilderImpl dataBuilder;

    @BeforeEach
    void setUp() {
        dataBuilder = new DataBuilderImpl(new StubTranslitConverter());
    }

    @Test
    void buildProfileUidNormalizesNames() {
        String uid = dataBuilder.buildProfileUid("John", "Doe");

        assertEquals("john-doe", uid);
    }

    @Test
    void buildProfileUidReturnsSingleNameWhenOtherIsEmpty() {
        String uid = dataBuilder.buildProfileUid(null, "Doe");

        assertEquals("doe", uid);
    }

    @Test
    void buildProfileUidStripsSpecialSymbolsAndCollapsesSeparators() {
        String uid = dataBuilder.buildProfileUid(" Jo@@@hn-- ", "  D##oe__ ");

        assertEquals("john-doe", uid);
    }

    @Test
    void buildProfileUidTruncatesToMaxLength() {
        String uid = dataBuilder.buildProfileUid("ABCDEFGHIJKLMNOPQRSTUVWXYZ", "ABCDEFGHIJKLMNOPQRSTUVWXYZ");

        assertEquals(50, uid.length());
        assertTrue(uid.startsWith("abcdefghijklmnopqrstuvwxyz-"));
    }

    @Test
    void buildProfileUidReturnsEmptyWhenNormalizedValueIsEmpty() {
        String uid = dataBuilder.buildProfileUid("@@@", "###");

        assertEquals("", uid);
    }

    private static final class StubTranslitConverter implements TranslitConverter {

        @Override
        public String translit(String text) {
            return text;
        }
    }
}
