#!/usr/bin/env bash
docker run --rm -d -e SERVICE_NAME="service-$1" --name "service-$1" -p "$1:80" nginx:latest
