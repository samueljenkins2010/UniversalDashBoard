package com.sjenkins;

import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.consul.ConsulContainer;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;

@SpringBootTest
public class ApplicationIT {

    @ClassRule
    public static ConsulContainer consulContainer = new ConsulContainer("hashicorp/consul:1.16");

    @ClassRule
    public static GenericContainer grafanaContainer = new GenericContainer("grafana/grafana:11.3.0")
            .withExposedPorts(3000)
            .withEnv("GF_DEFAULT_APP_MODE", "development")
            .withEnv("GF_AUTH_ANONYMOUS_ORG_ROLE", "Admin")
            .withEnv("GF_AUTH_ANONYMOUS_ENABLED", "true")
            .withEnv("GF_AUTH_BASIC_ENABLED", "false")
            .withEnv("GF_FEATURE_TOGGLES_ENABLE", "alertingSimplifiedRouting,alertingQueryAndExpressionsStepMode");

    @Test
    public void registersNewContainers() throws InterruptedException, IOException {
        consulContainer.execInContainer("consul services register -name loadbalancer-5080");
    }
}
