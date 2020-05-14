#!/bin/sh
set -e

host=$1
port=$2
echo -n "Waiting for TCP connection to $host:$port..."

while ! nc -z $host $port; do
  echo -n .
  sleep 1
done

echo 'Port available'
