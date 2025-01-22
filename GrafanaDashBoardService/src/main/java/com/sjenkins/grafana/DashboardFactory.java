package com.sjenkins.grafana;

import org.springframework.stereotype.Component;
import uk.co.szmg.grafana.domain.Dashboard;
import uk.co.szmg.grafana.domain.DomainFactories;

@Component
public class DashboardFactory {
    public static final String DASHBOARD_TITLE = "%s Dashboard";
    public static final String TARGET_CPU_TITLE = "CPU Usage [1m]";
    private static final String TARGET_CPU = "rate(container_cpu_usage_seconds_total{name=\"%s\"}[1m])";
    public static final String TARGET_RAM_TITLE = "RAM Usage [1m]";
    private static final String TARGET_RAM = "rate(container_memory_usage_bytes{name=\"%s\"}[1m])";

    public Dashboard makeDashboard(final String service) {
        return DomainFactories.newDashboard().withTitle(String.format(DASHBOARD_TITLE, service))
                .withTimezone("browser")
                .addRow(DomainFactories.newRow()
                        .withHeight("200px")
                        .addPanel(DomainFactories.newGraph()
                                .withLines(true).withTitle(TARGET_CPU_TITLE)
                                .withType("timeseries")
                                .addTarget(DomainFactories.newTarget()
                                        .withValue("legendFormat", "__auto")
                                        .withValue("expr", String.format(TARGET_CPU, service))
                                        .withValue("range", true)).withSpan(12)))
                .addRow(DomainFactories.newRow()
                        .withHeight("200px")
                        .addPanel(DomainFactories.newGraph()
                                .withLines(true)
                                .withType("timeseries")
                                .withTitle(TARGET_RAM_TITLE)
                                .addTarget(DomainFactories.newTarget()
                                        .withValue("legendFormat", "__auto")
                                        .withValue("expr", String.format(TARGET_RAM, service))
                                        .withValue("range", true)).withSpan(12)));
    }

}
