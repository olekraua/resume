package net.devstudy.resume.web.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class UiPropertiesTest {

    @Test
    void storesUiPropertiesValues() {
        UiProperties properties = new UiProperties();
        properties.setProduction(true);
        properties.setHost("https://example.com");
        properties.setMaxProfilesPerPage(20);

        UiProperties.Versions versions = new UiProperties.Versions();
        versions.setCssCommon("css-common");
        versions.setCssEx("css-ex");
        versions.setJsCommon("js-common");
        versions.setJsEx("js-ex");
        versions.setJsMessages("js-msg");
        properties.setVersions(versions);

        assertEquals(true, properties.isProduction());
        assertEquals("https://example.com", properties.getHost());
        assertEquals(Integer.valueOf(20), properties.getMaxProfilesPerPage());
        assertEquals("css-common", properties.getVersions().getCssCommon());
        assertEquals("css-ex", properties.getVersions().getCssEx());
        assertEquals("js-common", properties.getVersions().getJsCommon());
        assertEquals("js-ex", properties.getVersions().getJsEx());
        assertEquals("js-msg", properties.getVersions().getJsMessages());
    }

    @Test
    void createsDefaultVersions() {
        UiProperties properties = new UiProperties();

        assertNotNull(properties.getVersions());
    }
}
