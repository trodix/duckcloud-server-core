#!/bin/bash

read -p "Version: " VERSION
read -p "SSH credentials: " SSH_CREDS

if [[ -z $VERSION ]];
then
  echo "[Version] is a mandatory parameter."
  exit 1;
fi;

if [[ -z $SSH_CREDS ]];
then
  echo "[SSH credentials] is a mandatory parameter."
  exit 1;
fi;

mvn -B clean package -DskipTests

docker build . -t ghcr.io/trodix/duckcloud-server:$VERSION

docker save ghcr.io/trodix/duckcloud-server:$VERSION | ssh -C $SSH_CREDS docker load

