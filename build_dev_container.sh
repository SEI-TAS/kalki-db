#!/usr/bin/env bash

# Pass proxy info, if any, to gradle inside the docker first stage.
IFS=':' read PROXY_HOST PROXY_PORT <<<"$(echo ${http_proxy/http:\/\//})"
echo -en "systemProp.http.proxyHost=${PROXY_HOST}\nsystemProp.http.proxyPort=${PROXY_PORT}\n" >> gradle.properties
echo -en "systemProp.https.proxyHost=${PROXY_HOST}\nsystemProp.https.proxyPort=${PROXY_PORT}\n" >> gradle.properties

docker build -t kalki/kalki-db-env -f Dockerfile.build .

rm gradle.properties
