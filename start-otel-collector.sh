#!/bin/sh
set -e

CDIR=$(dirname $0)
GOOGLE_ADC_FILE="$HOME/.config/gcloud/xtc-test-cloudstorage.json"

exec \
  docker run --rm \
    -v $CDIR/otel-collector-config.yaml:/etc/otel-collector-config.yaml \
    -v "$GOOGLE_ADC_FILE:/etc/google_credentials.json" \
    -e "GOOGLE_APPLICATION_CREDENTIALS=/etc/google_credentials.json" \
    -p 127.0.0.1:14318:4318 otel/opentelemetry-collector-contrib \
    --config /etc/otel-collector-config.yaml
