#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

set -e

MVNW_ROOT="$(cd "$(dirname "$0")"; pwd -P)"

if [ -z "$JAVA_HOME" ]; then
  JAVA_EXE="$(command -v java)"
  if [ -z "$JAVA_EXE" ]; then
    echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH." >&2
    exit 1
  fi
else
  JAVA_EXE="$JAVA_HOME/bin/java"
fi

WRAPPER_JAR="$MVNW_ROOT/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_PROPERTIES="$MVNW_ROOT/.mvn/wrapper/maven-wrapper.properties"

exec "$JAVA_EXE" -Dmaven.multiModuleProjectDirectory="$MVNW_ROOT" \
  -cp "$WRAPPER_JAR" org.apache.maven.wrapper.MavenWrapperMain "$@"

