#!/bin/bash

SCRIPT_FOLDER="sql/device_types"
TO_LOAD_FOLDER="${SCRIPT_FOLDER}/to_load"

# Copy only device types to be loaded into temp folder.
rm -r ${TO_LOAD_FOLDER}
mkdir ${TO_LOAD_FOLDER}
if [ "$#" -lt 1 ]; then
  if [ "${LOAD_EMPTY_MODE}" = "all" ]; then
    # If no argument given, copy all device types.
    echo "Loading ALL device types."
    cp ${SCRIPT_FOLDER}/*.sql ${TO_LOAD_FOLDER}/
  else
    # If no argument given, copy NO device types.
    echo "Not loading device types."
  fi
else
  # Copies all device types passed as parameters to temp folder.
  for TYPE in "$@"; do
    cp "${SCRIPT_FOLDER}/1-${TYPE}.sql" "${TO_LOAD_FOLDER}/"
  done
fi
