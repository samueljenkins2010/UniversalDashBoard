#!/usr/bin/env bash
which docker &> /dev/null
if [[ $? -ne 0 ]]; then
  echo "Docker is required, install docker by one of the following options:"
  echo "With snap:    snap install docker"
  echo "With apt-get: apt-get install docker.io"
  exit 1
fi

docker compose down -v
