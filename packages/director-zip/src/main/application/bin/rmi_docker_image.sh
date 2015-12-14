#!/bin/sh

REP=$1
TAG=$2
IMAGE_ID=$(docker images --no-trunc | awk -v rep="$REP" '$1 == rep' | awk -v tag="$TAG" '$2 == tag {print $3}')
docker rmi -f $IMAGE_ID
