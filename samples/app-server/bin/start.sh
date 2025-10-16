#!/bin/sh

# go to app-server root directory
cd "`dirname "$0"`/.."

# create the log directory
mkdir -p logs

# setup Java options
JAVA_OPTIONS=$JAVA_OPTIONS\ -Xmx512m
JAVA_OPTIONS=$JAVA_OPTIONS\ --add-opens=java.base/java.lang=ALL-UNNAMED

# fix #35/#382
JAVA_OPTIONS=$JAVA_OPTIONS\ -Djava.security.egd=file:/dev/./urandom
 
# run Java
java $JAVA_OPTIONS -jar start.jar
