#!/bin/bash

if [ "$1" == "--reset" ]; then
  echo "Reset requested, removing existing DB volume if any..."
  docker volume rm kalki-pgdata
fi

export HOST_TZ=$(cat /etc/timezone)
docker-compose up -d

# Show logs.
bash compose_logs.sh
