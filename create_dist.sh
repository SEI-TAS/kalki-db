#!/usr/bin/env bash

DIST_PATH=$1

if [ -z "${DIST_PATH}" ]; then
  echo "Destination dist path argument required"
  exit 1
fi

# Copy startup script, and ovs bridge scripts.
cp prepare_env.sh ${DIST_PATH}
