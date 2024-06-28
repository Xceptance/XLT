#!/bin/sh
set -e

CDIR=$(dirname $0)

exec docker run --rm -v $CDIR/otel-collector-config.yaml:/etc/otel-collector-config.yaml -p 127.0.0.1:14318:4318 otel/opentelemetry-collector --config /etc/otel-collector-config.yaml 
