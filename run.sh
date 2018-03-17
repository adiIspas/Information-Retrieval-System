#!/usr/bin/env bash
CURRENT_VERSION=$1

cd core
mvn package
cd ..
java -jar core/target/core-${CURRENT_VERSION}-SNAPSHOT.jar
