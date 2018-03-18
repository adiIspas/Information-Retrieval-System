#!/usr/bin/env bash
CURRENT_VERSION=$1

cd core
mvn clean package
cd ..
java -jar core/target/core-${CURRENT_VERSION}-SNAPSHOT.jar
