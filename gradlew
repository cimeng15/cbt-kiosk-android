#!/bin/sh
# Gradle wrapper script
APP_HOME=$( cd "${APP_HOME:-$(dirname "$0")}" > /dev/null && pwd -P ) || exit
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar
exec java $JAVA_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
