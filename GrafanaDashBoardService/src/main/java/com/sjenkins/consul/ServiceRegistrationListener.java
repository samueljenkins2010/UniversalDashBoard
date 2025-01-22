package com.sjenkins.consul;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.catalog.CatalogServicesRequest;
import com.sjenkins.grafana.DashboardFactory;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.consul.discovery.ConsulCatalogWatch;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.co.szmg.grafana.DashboardUploader;
import uk.co.szmg.grafana.GrafanaClient;

import java.util.List;
import java.util.Map;

@Component
public class ServiceRegistrationListener {
    @Autowired
    private ConsulClient client;

    @Autowired
    private DashboardUploader uploader;

    @Autowired
    private ConsulDiscoveryProperties properties;

    @Autowired
    private DashboardFactory dashboardFactory;

    @EventListener
    public void handleConsulEvent(HeartbeatEvent event) {
        System.out.println("Received a " + event.getSource().getClass() + " event: " + event.getValue());
        if (event.getSource() instanceof ConsulCatalogWatch && event.getValue() != null) {
            final Long index = (Long) event.getValue();
            Response<Map<String, List<String>>> response = client.getCatalogServices(buildRequest(index));
            for (Map.Entry<String, List<String>> service : response.getValue().entrySet()) {
                System.out.println(service.getKey() + " -> " + service.getValue().stream().reduce("", (left, right) -> String.join(",", left, right)));
                try {
                    uploader.upload(dashboardFactory.makeDashboard(service.getKey()), false);
                } catch (GrafanaClient.UnexpectedGrafanaResponseException e) {
                    if (e.getResponseCode() == HttpStatus.SC_PRECONDITION_FAILED) {
                        System.out.println(service.getKey() + " service is already registered.");
                    } else {
                        System.err.println(service.getKey() + " could not be uploaded, swallowing.");
                        System.err.println(e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private CatalogServicesRequest buildRequest(final Long index) {
        return CatalogServicesRequest.newBuilder().setQueryParams(QueryParams.Builder.builder().setIndex(index).build()).setToken(properties.getAclToken()).build();
    }
}
