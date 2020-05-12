#!/bin/bash

bash build_test_container.sh

docker container stop kalki-postgres-test
docker run -p 5433:5432 --rm -d --name kalki-postgres-test kalki/kalki-postgres-test
