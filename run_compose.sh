#!/bin/bash

if [ "$1" == "--reset" ]; then
  echo "Reset requested, removing existing DB volume if any..."
  VOLUME=$(docker container inspect kalki-postgres | grep -o -P '(?<=Name": ").*(?<=kalki-pgdata)')
  docker volume rm $VOLUME
fi

export HOST_TZ=$(cat /etc/timezone)
docker-compose up -d
