#!/bin/bash
export HOST_TZ=$(cat /etc/timezone)
docker-compose up -d
