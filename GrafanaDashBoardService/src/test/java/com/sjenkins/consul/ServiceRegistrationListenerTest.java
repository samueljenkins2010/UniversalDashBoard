package com.sjenkins.consul;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.catalog.CatalogServicesRequest;
import com.sjenkins.grafana.DashboardFactory;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.consul.discovery.ConsulCatalogWatch;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.co.szmg.grafana.DashboardUploader;
import uk.co.szmg.grafana.GrafanaClient;
import uk.co.szmg.grafana.domain.Dashboard;
import uk.co.szmg.grafana.domain.DomainFactories;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

@SpringBootTest
public class ServiceRegistrationListenerTest {
    @MockitoBean
    private ConsulClient client;

    @MockitoBean
    private DashboardUploader uploader;

    @MockitoBean
    private DashboardFactory factory;

    @MockitoBean
    private ConsulCatalogWatch watcher;

    @Autowired
    private ServiceRegistrationListener listener;

    @BeforeEach
    public void cleanup() {
        clearInvocations(client, factory, uploader);
    }

    @Test
    public void handlesConsulEventsTest() {
        final String service = "consul";
        final long consulIndex = 11;
        final Dashboard dashboard = DomainFactories.newDashboard();
        when(client.getCatalogServices(any(CatalogServicesRequest.class))).thenReturn(successful(service, consulIndex));
        when(factory.makeDashboard(any(String.class))).thenReturn(dashboard);

        HeartbeatEvent event = new HeartbeatEvent(watcher, consulIndex);
        listener.handleConsulEvent(event);

        verify(client).getCatalogServices(any(CatalogServicesRequest.class));
        verify(factory).makeDashboard(service);
        verify(uploader).upload(eq(dashboard), eq(false));
        verifyNoMoreInteractions(client, factory, uploader);
    }

    @Test
    public void handlesDuplicateDashboardTest() {
        final String service = "consul";
        final long consulIndex = 11;
        final Dashboard dashboard = DomainFactories.newDashboard();
        when(client.getCatalogServices(any(CatalogServicesRequest.class))).thenReturn(successful(service, consulIndex));
        when(factory.makeDashboard(any(String.class))).thenReturn(dashboard);
        doThrow(new GrafanaClient.UnexpectedGrafanaResponseException("", HttpStatus.SC_PRECONDITION_FAILED, "")).when(uploader).upload(any(Dashboard.class), anyBoolean());

        HeartbeatEvent event = new HeartbeatEvent(watcher, consulIndex);
        listener.handleConsulEvent(event);

        verify(client).getCatalogServices(any(CatalogServicesRequest.class));
        verify(factory).makeDashboard(service);
        verify(uploader).upload(eq(dashboard), eq(false));
        verifyNoMoreInteractions(client, factory, uploader);
    }

    @Test
    public void handlesGrafanaErrorsTest() {
        final String service = "consul";
        final long consulIndex = 11;
        final Dashboard dashboard = DomainFactories.newDashboard();
        when(client.getCatalogServices(any(CatalogServicesRequest.class))).thenReturn(successful(service, consulIndex));
        when(factory.makeDashboard(any(String.class))).thenReturn(dashboard);
        doThrow(new GrafanaClient.UnexpectedGrafanaResponseException("", HttpStatus.SC_SERVICE_UNAVAILABLE, "")).when(uploader).upload(any(Dashboard.class), anyBoolean());

        HeartbeatEvent event = new HeartbeatEvent(watcher, consulIndex);
        listener.handleConsulEvent(event);

        verify(client).getCatalogServices(any(CatalogServicesRequest.class));
        verify(factory).makeDashboard(service);
        verify(uploader).upload(eq(dashboard), eq(false));
        verifyNoMoreInteractions(client, factory, uploader);
    }

    public Response<Map<String, List<String>>> successful(String service, long index) {
        return new Response(Collections.singletonMap(service, Collections.emptyList()), index, true, 0L);
    }
}
