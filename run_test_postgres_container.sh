#!/bin/bash

SCRIPT_FOLDER="sql/device_types"
TO_LOAD_FOLDER="${SCRIPT_FOLDER}/to_load"

# Copy only device types to be loaded into temp folder.
rm -r ${TO_LOAD_FOLDER}
mkdir ${TO_LOAD_FOLDER}
if [ "$#" -lt 1 ]
then
  # If no argument given, copy NO device types.
  echo "Not loading device types."
else
  # Copies all device types passed as parameters to temp folder.
  for TYPE in "$@"
  do
    cp "${SCRIPT_FOLDER}/1-${TYPE}.sql" "${TO_LOAD_FOLDER}/"
  done
fi

docker container stop kalki-postgres-test
docker build -t kalki/kalki-postgres-test -f Dockerfile.test .
docker run -p 5433:5432 --rm -d --name kalki-postgres-test kalki/kalki-postgres-test
