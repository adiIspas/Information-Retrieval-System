#!/usr/bin/env bash
CURRENT_VERSION=$1

java -Dspring.profiles.active=index-docs -jar core/target/core-${CURRENT_VERSION}-SNAPSHOT.jar
