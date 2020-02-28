#!/bin/bash

# Select here which device types to load.
declare -a TO_LOAD=("dlc" "phle" "unts" "wemo")

# Copies all device types to be loaded to temp folder.
rm sql/device_types/to_load/*
for TYPE in "${TO_LOAD[@]}"
do
	cp "sql/device_types/1-${TYPE}.sql" "sql/device_types/to_load/"
done

docker container stop kalki-postgres
docker build -t kalki/kalki-postgres .
docker run -p 5432:5432 --rm -d --name kalki-postgres kalki/kalki-postgres
