#!/usr/bin/env bash

# Start up test DB.
bash run_test_postgres_container.sh

# Wait for the test DB to be up.
bash wait_for_postgres.sh kalki-postgres-test

# Pass proxy info, if any, to gradle inside the docker first stage.
IFS=':' read PROXY_HOST PROXY_PORT <<<"$(echo ${http_proxy/http:\/\//})"
echo -en "systemProp.http.proxyHost=${PROXY_HOST}\nsystemProp.http.proxyPort=${PROXY_PORT}\n" >> gradle.properties
echo -en "systemProp.https.proxyHost=${PROXY_HOST}\nsystemProp.https.proxyPort=${PROXY_PORT}\n" >> gradle.properties

docker build --network=host -t kalki/kalki-db-env -f Dockerfile.dev .

rm gradle.properties
