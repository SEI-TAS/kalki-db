#!/bin/bash

test_db_name='kalki-postgres-test'
docker container stop $test_db_name
sleep 1
docker run -p 5433:5432 --rm -d --name $test_db_name kalki/$test_db_name
