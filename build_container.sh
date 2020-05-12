#!/bin/bash

LOAD_EMPTY_MODE="all"
source load_device_types.sh

docker build -t kalki/kalki-postgres .
