package net.devstudy.resume.component.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SimpleTranslitConverterTest {

    private final SimpleTranslitConverter converter = new SimpleTranslitConverter();

    @Test
    void translitReturnsEmptyForNullOrEmpty() {
        assertEquals("", converter.translit(null));
        assertEquals("", converter.translit(""));
    }

    @Test
    void translitKeepsAsciiAsIs() {
        assertEquals("John Doe-123", converter.translit("John Doe-123"));
    }

    @Test
    void translitConvertsCyrillicToLatin() {
        assertEquals("Pryvit", converter.translit("Привіт"));
        assertEquals("Kyiv", converter.translit("Київ"));
    }

    @Test
    void translitPreservesUppercaseForMappedCharacters() {
        assertEquals("Ya", converter.translit("Я"));
        assertEquals("Shch", converter.translit("Щ"));
        assertEquals("Ye", converter.translit("Є"));
    }

    @Test
    void translitDropsSoftAndHardSigns() {
        assertEquals("", converter.translit("ьъ"));
    }

    @Test
    void translitSkipsUnknownNonAsciiCharacters() {
        assertEquals("", converter.translit("漢字"));
    }
}
