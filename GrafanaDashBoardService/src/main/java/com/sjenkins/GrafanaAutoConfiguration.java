package com.sjenkins;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.co.szmg.grafana.DashboardUploader;
import uk.co.szmg.grafana.GrafanaEndpoint;

@Configuration
public class GrafanaAutoConfiguration {

    @ConfigurationProperties("grafana")
    private class GrafanaProperties {
        private String baseUrl = "";
        private String apiKey = "";
        private Boolean skipSSLValidation = false;

        public String getBaseUrl() {
            return baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public Boolean getSkipSSLValidation() {
            return skipSSLValidation;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public void setSkipSSLValidation(Boolean skipSSLValidation) {
            this.skipSSLValidation = skipSSLValidation;
        }
    }

    @Bean
    public GrafanaProperties grafanaConfiguration() {
        return new GrafanaProperties();
    }

    @Bean
    public GrafanaEndpoint grafanaEndpoint(final GrafanaProperties grafanaProperties) {
        final GrafanaEndpoint endpoint = new GrafanaEndpoint();
        endpoint.setApiKey(grafanaProperties.getApiKey());
        endpoint.setBaseUrl(grafanaProperties.getBaseUrl());
        endpoint.setSkipSSLValidation(grafanaProperties.getSkipSSLValidation());
        return endpoint;
    }

    @Bean
    public DashboardUploader dashboardUploader(final GrafanaEndpoint grafanaEndpoint) {
        return new DashboardUploader(grafanaEndpoint);
    }
}
