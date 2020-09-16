#!/bin/bash

LOAD_EMPTY_MODE="all"

# Avoiding issue when passing skip tests param.
if [ "$1" == "--skip_tests" ]; then
  source load_device_types.sh
else
  source load_device_types.sh "$@"
fi

docker build -t kalki/kalki-postgres .
