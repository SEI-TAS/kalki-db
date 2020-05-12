#!/bin/bash

LOAD_EMPTY_MODE="none"
source load_device_types.sh

docker build -t kalki/kalki-postgres-test -f Dockerfile.test .

docker container stop kalki-postgres-test
docker run -p 5433:5432 --rm -d --name kalki-postgres-test kalki/kalki-postgres-test
