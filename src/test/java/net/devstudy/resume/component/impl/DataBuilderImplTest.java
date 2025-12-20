package net.devstudy.resume.component.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.devstudy.resume.component.TranslitConverter;

class DataBuilderImplTest {

    private DataBuilderImpl dataBuilder;

    @BeforeEach
    @SuppressWarnings("unused")
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
        String uid = dataBuilder.buildProfileUid("A".repeat(40), "B".repeat(40));

        assertEquals(64, uid.length());
        assertTrue(uid.startsWith("a".repeat(40) + "-"));
    }

    @Test
    void buildProfileUidReturnsEmptyWhenNormalizedValueIsEmpty() {
        String uid = dataBuilder.buildProfileUid("@@@", "###");

        assertEquals("", uid);
    }

    @Test
    void buildProfileUidTrimsLeadingAndTrailingDelimiters() {
        String uid = dataBuilder.buildProfileUid(" -John- ", " --Doe-- ");

        assertEquals("john-doe", uid);
    }

    @Test
    void buildRestoreAccessLinkConcatenatesWithRestorePath() {
        String link = dataBuilder.buildRestoreAccessLink("http://example.com", "token123");

        assertEquals("http://example.com/restore/token123", link);
    }

    @Test
    void buildRestoreAccessLinkKeepsTrailingSlashBehavior() {
        String link = dataBuilder.buildRestoreAccessLink("http://example.com/", "token123");

        assertEquals("http://example.com//restore/token123", link);
    }

    @Test
    void rebuildUidWithRandomSuffixUsesRequestedLengthAndAlphabet() {
        String baseUid = "john-doe";
        String alphabet = "abc";

        String uid = dataBuilder.rebuildUidWithRandomSuffix(baseUid, alphabet, 8);

        assertTrue(uid.startsWith(baseUid + "-"));
        assertEquals(baseUid.length() + 1 + 8, uid.length());
        String suffix = uid.substring(baseUid.length() + 1);
        for (int i = 0; i < suffix.length(); i++) {
            assertTrue(alphabet.indexOf(suffix.charAt(i)) >= 0);
        }
    }

    @Test
    void rebuildUidWithRandomSuffixKeepsTrailingDelimiterWhenCountIsNotPositive() {
        String uid = dataBuilder.rebuildUidWithRandomSuffix("john-doe", "abc", 0);

        assertEquals("john-doe-", uid);
    }

    @Test
    void buildCertificateNameReturnsEmptyForNullOrBlank() {
        assertEquals("", dataBuilder.buildCertificateName(null));
        assertEquals("", dataBuilder.buildCertificateName(""));
    }

    @Test
    void buildCertificateNameStripsExtensionAndCapitalizes() {
        String name = dataBuilder.buildCertificateName("file.pdf");

        assertEquals("File", name);
    }

    @Test
    void buildCertificateNameReplacesSeparatorsWithSpaces() {
        String name = dataBuilder.buildCertificateName("my_cert-file.pdf");

        assertEquals("My Cert File", name);
    }

    @Test
    void buildCertificateNameReturnsEmptyWhenNormalizedIsEmpty() {
        String name = dataBuilder.buildCertificateName("###.pdf");

        assertEquals("", name);
    }

    @Test
    void buildCertificateNameCapitalizesEachWord() {
        String name = dataBuilder.buildCertificateName("multi word name.pdf");

        assertEquals("Multi Word Name", name);
    }

    private static final class StubTranslitConverter implements TranslitConverter {

        @Override
        public String translit(String text) {
            return text;
        }
    }
}
