package com.sjenkins.grafana;

import org.junit.jupiter.api.Test;
import uk.co.szmg.grafana.domain.Dashboard;
import uk.co.szmg.grafana.domain.Row;
import uk.co.szmg.grafana.domain.Panel;
import uk.co.szmg.grafana.domain.Graph;
import uk.co.szmg.grafana.domain.Target;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class DashboardFactoryTest {
    final String service = "test";

    final Dashboard dashboard = new DashboardFactory().makeDashboard(service);

    @Test
    public void makesADashboardTest() {
        assertNotNull(dashboard);
        assertEquals(service + " Dashboard", dashboard.getTitle());
    }

    @Test
    public void dashboardHasTwoRowsOf200pxInHeightTest() {
        Collection<Row> rows = dashboard.getRows();

        assertEquals(2, rows.size());
        for (Row row : rows) {
            assertEquals("200px", row.getHeight());
        }
    }

    @Test
    public void eachRowsPanelAreGraphingRamOrCpuMetricsForServiceTest() {
        List<Panel> panels = dashboard.getRows().stream().flatMap(row -> row.getPanels().stream()).toList();
        assertEquals(2, panels.size());
        assertTrue(panels.stream().anyMatch(panel -> DashboardFactory.TARGET_CPU_TITLE.equals(panel.getTitle())));
        assertTrue(panels.stream().anyMatch(panel -> DashboardFactory.TARGET_RAM_TITLE.equals(panel.getTitle())));

        Graph graph;
        List<Target> targets;
        Target target;
        for (Panel panel : panels) {
            assertTrue(panel instanceof Graph);

            graph = (Graph) panel;
            assertTrue(graph.getLines());
            assertEquals("timeseries", panel.getType());

            targets = graph.getTargets();
            assertEquals(1, targets.size());

            target = targets.get(0);
            assertEquals("__auto", target.getValue("legendFormat"));
            assertNotNull(target.getValue("expr"));
            assertTrue(target.getValue("expr").toString().contains(service));
            assertEquals(true, target.getValue("range"));
        }
    }
}
