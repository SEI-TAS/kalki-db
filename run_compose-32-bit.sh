#!/bin/bash

if [ "$1" == "--reset" ]; then
  echo "Reset requested, removing existing DB volume if any..."
  docker volume rm kalki-pgdata
fi

export HOST_TZ=$(cat /etc/timezone)
docker-compose -f docker-compose-32-bit.yml up -d
