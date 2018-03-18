#!/usr/bin/env bash
CURRENT_VERSION=$1

mvn clean package
java -jar core/target/core-${CURRENT_VERSION}-SNAPSHOT.jar
