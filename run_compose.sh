#!/bin/bash
export HOST_TZ=$(cat /etc/timezone)
export CURRENT_UID=$(id -u):$(id -g)
docker-compose up -d
