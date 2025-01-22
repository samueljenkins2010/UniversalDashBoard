# Universal Dashboard

This project runs a Java service in Docker that automatically registers dashboards for containers added to Consul's Service Discovery.

The following components make up the system design:

![System Overview](/Drawings/SystemOverview.PNG)

The two flows are as follows:

### Metrics Collation Flow
(a) Docker Daemon's updates its system state as new containers are provisioned, these are detected by cAdvisor and added to its metrics;  
(b) cAdvisor's metrics are scraped by Prometheus and collated into Prometheus' inmemory store;  
(c) Grafana updates its dashboards with the metrics data from Prometheus.

### Dashboard Registry
(1) Registrator tracks events from the Docker Daemon, when a container is provisioned then Registrator evaluates the labelling and environment variables on the container for SERVICE_NAME or SERVICE_[PORT]_NAME;  
(2) Containers are registered as services on Consul with the name set in the SERVICE_NAME or SERVICE_[PORT]_NAME label/variable;  
(3) The Dashboard Service watches Consul and receives updates when Consul Services are updated;  
(4) The Dashboard Service adds a Dashboard to Grafana for newly created containers, on the next update Grafana will collate the data and display them on the configured Dashboard.

## Assumptions
- Additional Containers are provisioned by operators outside of the system, a [script](add-service.sh) has been provided to add nginx services for monitoring;
- Containers that are provisioned and require dashboards must have a label or environment variable SERVICE_NAME or SERVICE_[PORT]_NAME that matches the container name with an exposed port;
- Docker client and Daemon are the bare minimum installed applications on a Ubuntu LTS distribution.

## Dependencies

- [Grafana](https://grafana.com/grafana/);
- [Prometheus](https://prometheus.io/);
- [Consul](https://www.consul.io/);
- [cAdvisor](https://github.com/google/cadvisor?tab=readme-ov-file);
- [Docker](https://www.docker.com/);
- [Java OpenJDK 17](https://openjdk.org/);
- [Apache Maven](https://maven.apache.org/);
- [Spring](https://spring.io/);
- [Grafana dashboard builder in Java (WIP)](https://github.com/szmg/grafana-dashboard-generator-java/tree/master)
- [Ubuntu 20.04.6 LTS (Focal Fossa)](https://www.releases.ubuntu.com/focal/);
- [For Windows, WSL2](https://learn.microsoft.com/en-us/windows/wsl/about#what-is-wsl-2)

## Outstanding Work

The following ideas would be possible avenues to improve the application:

- Add SSL/TLS between containers and outside the container;
- Secure networking to restrict access between the containers;
- Add authentication and authorisation for access to Consul, Prometheus and Grafana services;
- Add external data storage for Prometheus;
- Add Log Aggregation;
- Support for an exposed Docker Daemon, with Security hardened public access (TLS, Auth, etc.);
- Full Integration Testing with testcontainers, currently the integration test is not hooked up nor completed;
- Move to a more integrated Grafana Library;
- Add logic to separate dependency on container name matching the consul service name;
- Add logic to customise dashboards for containers via container labels or environment variables;
- Support adding custom Grafana datasources from containers;
- Integrate Consul Metrics with Grafana;
- Move hosting into a Cloud Provider;
- Update to use a HA Architecture;
- Add services that monitor data on real-time, such as the stock market or weather, perhaps using websockets if traffic is high enough;
- Add notifications with message queue solutions to propagate alerts reliably to any onward service platform;
- Move over to Kubernetes and configure this all in a Helm Chart or Kustomize.
