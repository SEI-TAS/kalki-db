#!/usr/bin/env bash

DIST_PATH=$1

if [ -z "${DIST_PATH}" ]; then
  echo "Destination dist path argument required"
  exit 1
fi

# Copy startup script, and ovs bridge scripts.
cp docker-logs.sh ${DIST_PATH}
cp run_postgres_container.sh ${DIST_PATH}
cp stop_container.sh ${DIST_PATH}
