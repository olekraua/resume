package net.devstudy.resume.shared.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class LanguageLevelTest {

    @ParameterizedTest
    @EnumSource(LanguageLevel.class)
    void sliderAndDbValuesMatchEnum(LanguageLevel level) {
        assertEquals(level.ordinal(), level.getSliderIntValue());
        assertEquals(level.name().toLowerCase(), level.getDbValue());
    }

    @Test
    void converterRoundTripsValue() {
        LanguageLevel.PersistJPAConverter converter = new LanguageLevel.PersistJPAConverter();

        String dbValue = converter.convertToDatabaseColumn(LanguageLevel.UPPER_INTERMEDIATE);
        LanguageLevel restored = converter.convertToEntityAttribute(dbValue);

        assertEquals(LanguageLevel.UPPER_INTERMEDIATE, restored);
    }
}
