#!/usr/bin/env bash
which docker &> /dev/null
if [[ $? -ne 0 ]]; then
  echo "Docker is required, install docker by one of the following options:"
  echo "- With snap:    sudo snap install docker"
  echo "- With apt-get: sudo apt-get install docker.io"
  exit 1
fi

if [[ -z "$(docker images | grep dashboard-service)" ]]; then
  maven_args=( mvn clean spring-boot:build-image -DskipTests )

  which mvn &> /dev/null
  if [[ $? -ne 0 ]]; then
    docker run -it --rm --name maven-builder \
      -v ~/.m2:/root/.m2 \
      -v ./GrafanaDashBoardService:/project \
      -v /var/run/docker.sock:/var/run/docker.sock \
      -e DOCKER_HOST=unix:///var/run/docker.sock \
      -w /project \
      maven:3.8.5-openjdk-17-slim "${maven_args[@]}"
  else
    pushd GrafanaDashBoardService
    mvn "${maven_args[@]}"
    popd
  fi
fi

docker compose up -d
