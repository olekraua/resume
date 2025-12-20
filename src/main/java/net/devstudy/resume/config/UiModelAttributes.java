package net.devstudy.resume.config;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class UiModelAttributes {

    private final UiProperties uiProperties;

    public UiModelAttributes(UiProperties uiProperties) {
        this.uiProperties = uiProperties;
    }

    @ModelAttribute
    public void addUiAttributes(Model model) {
        UiProperties.Versions versions = uiProperties.getVersions();
        model.addAttribute("production", uiProperties.isProduction());
        model.addAttribute("appHost", uiProperties.getHost());
        model.addAttribute("applicationHost", uiProperties.getHost());
        model.addAttribute("cssCommonVersion", versions.getCssCommon());
        model.addAttribute("cssExVersion", versions.getCssEx());
        model.addAttribute("jsCommonVersion", versions.getJsCommon());
        model.addAttribute("jsExVersion", versions.getJsEx());
        model.addAttribute("jsMessagesVersion", versions.getJsMessages());
        model.addAttribute("uiMaxProfilesPerPage", uiProperties.getMaxProfilesPerPage());
    }
}
