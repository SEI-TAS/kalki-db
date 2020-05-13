#!/bin/bash
test_db_name='kalki-postgres-test'
[[ $(docker ps -f "name=$test_db_name" --format '{{.Names}}') == $test_db_name ]] || \
docker run -p 5433:5432 --rm -d --name $test_db_name kalki/$test_db_name