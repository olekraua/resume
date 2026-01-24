package net.devstudy.resume.web.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

class UiModelAttributesTest {

    @Test
    void addsUiAttributesToModel() {
        UiProperties properties = new UiProperties();
        properties.setProduction(true);
        properties.setHost("https://example.com");
        properties.setMaxProfilesPerPage(25);

        UiProperties.Versions versions = new UiProperties.Versions();
        versions.setCssCommon("css-common-v1");
        versions.setCssEx("css-ex-v1");
        versions.setJsCommon("js-common-v1");
        versions.setJsEx("js-ex-v1");
        versions.setJsMessages("js-msg-v1");
        properties.setVersions(versions);

        UiModelAttributes attributes = new UiModelAttributes(properties);
        ExtendedModelMap model = new ExtendedModelMap();

        attributes.addUiAttributes(model);

        assertEquals(Boolean.TRUE, model.get("production"));
        assertEquals("https://example.com", model.get("appHost"));
        assertEquals("https://example.com", model.get("applicationHost"));
        assertEquals("css-common-v1", model.get("cssCommonVersion"));
        assertEquals("css-ex-v1", model.get("cssExVersion"));
        assertEquals("js-common-v1", model.get("jsCommonVersion"));
        assertEquals("js-ex-v1", model.get("jsExVersion"));
        assertEquals("js-msg-v1", model.get("jsMessagesVersion"));
        assertEquals(25, model.get("uiMaxProfilesPerPage"));
    }
}
