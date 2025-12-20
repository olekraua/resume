package net.devstudy.resume.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ui")
public class UiProperties {

    private boolean production;
    private String host;
    private Integer maxProfilesPerPage;
    private Versions versions = new Versions();

    public boolean isProduction() {
        return production;
    }

    public void setProduction(boolean production) {
        this.production = production;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getMaxProfilesPerPage() {
        return maxProfilesPerPage;
    }

    public void setMaxProfilesPerPage(Integer maxProfilesPerPage) {
        this.maxProfilesPerPage = maxProfilesPerPage;
    }

    public Versions getVersions() {
        return versions;
    }

    public void setVersions(Versions versions) {
        this.versions = versions;
    }

    public static class Versions {
        private String cssCommon;
        private String cssEx;
        private String jsCommon;
        private String jsEx;
        private String jsMessages;

        public String getCssCommon() {
            return cssCommon;
        }

        public void setCssCommon(String cssCommon) {
            this.cssCommon = cssCommon;
        }

        public String getCssEx() {
            return cssEx;
        }

        public void setCssEx(String cssEx) {
            this.cssEx = cssEx;
        }

        public String getJsCommon() {
            return jsCommon;
        }

        public void setJsCommon(String jsCommon) {
            this.jsCommon = jsCommon;
        }

        public String getJsEx() {
            return jsEx;
        }

        public void setJsEx(String jsEx) {
            this.jsEx = jsEx;
        }

        public String getJsMessages() {
            return jsMessages;
        }

        public void setJsMessages(String jsMessages) {
            this.jsMessages = jsMessages;
        }
    }
}
