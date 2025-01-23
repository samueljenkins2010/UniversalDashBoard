# Universal Dashboard

This project runs a Java service in Docker that automatically registers dashboards for containers added to Consul's Service Discovery.

The following components make up the system design:

![System Overview](/Drawings/SystemOverview.PNG)

The two flows are as follows:

### Metrics Collation Flow
(a) Docker Daemon's updates its system state as new containers are provisioned, these are detected by cAdvisor and added to its metrics;  
(b) cAdvisor's metrics are scraped by Prometheus and collated into Prometheus' in memory store;  
(c) Grafana updates its dashboards with the metrics data from Prometheus.

### Dashboard Registry
(1) Registrator tracks events from the Docker Daemon, when a container is provisioned then Registrator evaluates the labelling and environment variables on the container for `SERVICE_NAME` or `SERVICE_<PORT>_NAME`;  
(2) Containers are registered as services on Consul with the name set in the `SERVICE_NAME` or `SERVICE_<PORT>_NAME` label/variable;  
(3) The Dashboard Service watches Consul and receives updates when Consul Services are updated;  
(4) The Dashboard Service adds a Dashboard to Grafana for newly created containers, on the next update Grafana will collate the data and display them on the configured Dashboard.

## Setup and run
This project is located in Github on my [personal account](https://github.com/samueljenkins2010/UniversalDashBoard), run the following instructions in bash to setup and run the program.

### Pre-requisites

- Git is required to run Git commands;
- Docker must be installed.

### Setup
Run the following to clone the repository with Git:
```shell
git clone https://github.com/samueljenkins2010/UniversalDashBoard.git
```
Otherwise a zip file of the repository can be downloaded and extracted to some directory on the system.

### Running the application
1. Navigate the to root of the local UniversalDashBoard repository;
2. Run the following:
```shell
./run-application.sh
```
3. Inspect the running containers with the following command:
```shell
$ docker ps
```
Expect similar output to the following:
```shell
CONTAINER ID   IMAGE                                  COMMAND                  CREATED          STATUS                            PORTS                                                                                         NAMES
e932db4eab17   metabrainz/serviceregistrator:v0.5.3   "serviceregistrator …"   14 seconds ago   Up 8 seconds                                                                                                                    registrator
3cbb1fd3ce16   prom/prometheus:v2.49.0                "/bin/prometheus --c…"   14 seconds ago   Up 8 seconds                      0.0.0.0:9090->9090/tcp, :::9090->9090/tcp                                                     prometheus
3233dc62c053   hashicorp/consul:1.16                  "docker-entrypoint.s…"   14 seconds ago   Up 9 seconds                      8300-8302/tcp, 8301-8302/udp, 8600/tcp, 8600/udp, 0.0.0.0:8500->8500/tcp, :::8500->8500/tcp   consul
48cb3ad7c1fe   dashboard-service:latest               "/cnb/process/web"       14 seconds ago   Up 9 seconds                                                                                                                    dashboard-service
afe5ca452a7d   grafana/grafana:11.3.0                 "/run.sh"                14 seconds ago   Up 9 seconds                      0.0.0.0:3000->3000/tcp, :::3000->3000/tcp                                                     grafana
c84ad89c0771   gcr.io/cadvisor/cadvisor:v0.47.2       "/usr/bin/cadvisor -…"   14 seconds ago   Up 8 seconds (health: starting)   0.0.0.0:8080->8080/tcp, :::8080->8080/tcp                                                     cadvisor
```

### Termination
Run the following at the root of the local UniversalDashBoard repository to terminate the running containers:
```shell
./terminate-application.sh
```

### Docker Snap installation

Snap installs docker in its own particular way. This version can be used, however the mount binding for cAdvisor to /var/lib/docker is missing on the host.
So it is recommended to append the following export statement to ~/.bashrc to point at the corrcet directory in snap:
```shell
export DOCKER_DATA_DIR=/var/snap/docker/common/var-lib-docker
```

### Windows Support

This application cannot be ran in a Docker Desktop environment due to issues with attaching the necessary system bind mounts for cAdvisor.
This issue has been with Docker Desktop for Windows for some time and older posts on the issue do not account for the more recent updates where the docker-desktop-data network drive is no longer in existence.
These complications mean that it is simpler to enable WSL2 and install docker on the lightweight VM provided.

Here are the issues that can inform on this particular issue:
- https://github.com/google/cadvisor/issues/2648
- https://github.com/vacp2p/wakurtosis/issues/58#issuecomment-2532341791

## Assumptions
- Additional Containers are provisioned by operators outside of the system, a [script](add-service.sh) has been provided to add nginx services for monitoring;
- Containers that are provisioned and require dashboards must have a label or environment variable `SERVICE_NAME` or `SERVICE_<PORT>_NAME` that matches the container name with an exposed port;
- Docker Client and Daemon are the bare minimum installed applications on a host computer running some Ubuntu LTS distribution.

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
