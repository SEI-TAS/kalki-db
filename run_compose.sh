#!/bin/bash
mkdir -p postgres-data
export HOST_TZ=$(cat /etc/timezone)
docker-compose up -d
