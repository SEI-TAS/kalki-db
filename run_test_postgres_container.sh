#!/bin/bash

docker container stop kalki-postgres-test
docker build -t kalki/kalki-postgres-test -f Dockerfile.test .
docker run -p 5433:5432 --rm -d --name kalki-postgres-test kalki/kalki-postgres-test
