#!/bin/bash
docker build -t kalki/kalki-postgres .
docker run -p 5432:5432 --rm -d --name kalki-postgres kalki/kalki-postgres
