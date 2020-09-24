#!/bin/bash

LOAD_EMPTY_MODE="none"
source load_device_types.sh "$@"

docker build -t kalki/kalki-postgres-test -f Dockerfile.test .
