#!/bin/bash

MAVEN_HOME="${TEMP}/apache-maven-3.9.5"

if [ ! -d "$MAVEN_HOME" ]; then
    echo "Downloading Apache Maven 3.9.5..."
    mkdir -p "$TEMP"
    cd "$TEMP"
    curl -o maven.zip https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.5/apache-maven-3.9.5-bin.zip
    unzip -q maven.zip
fi

export PATH="$MAVEN_HOME/bin:$PATH"

mvn "$@"
