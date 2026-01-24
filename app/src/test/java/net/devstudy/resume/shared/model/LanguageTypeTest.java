package net.devstudy.resume.shared.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class LanguageTypeTest {

    @ParameterizedTest
    @EnumSource(LanguageType.class)
    void dbValueUsesLowercase(LanguageType type) {
        assertEquals(type.name().toLowerCase(Locale.ROOT), type.getDbValue());
    }

    @Test
    void reverseTypeSwapsSpokenAndWriting() {
        assertEquals(LanguageType.WRITING, LanguageType.SPOKEN.getReverseType());
        assertEquals(LanguageType.SPOKEN, LanguageType.WRITING.getReverseType());
    }

    @Test
    void reverseTypeThrowsForAll() {
        assertThrows(IllegalArgumentException.class, () -> LanguageType.ALL.getReverseType());
    }

    @Test
    void converterHandlesNulls() {
        LanguageType.PersistJPAConverter converter = new LanguageType.PersistJPAConverter();

        assertNull(converter.convertToDatabaseColumn(null));
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void converterRoundTripsValue() {
        LanguageType.PersistJPAConverter converter = new LanguageType.PersistJPAConverter();

        String dbValue = converter.convertToDatabaseColumn(LanguageType.SPOKEN);
        LanguageType restored = converter.convertToEntityAttribute(dbValue);

        assertEquals(LanguageType.SPOKEN, restored);
    }
}
