#!/bin/bash
DIRECTOR_CONF=/opt/trustdirector/configuration
DIRECTOR_JAVA=/opt/trustdirector/java
DIRECTOR_JAVAFX_JAR=$(ls $DIRECTOR_JAVA/director-javafx*.jar | head -n 1)

JAVA_OPTS="-Dlogback.configurationFile=$DIRECTOR_CONF/logback.xml"

case "$1" in
#  help)
#    print_help
#    ;;
  start)
    
    java -jar $JAVA_OPTS $DIRECTOR_JAVAFX_JAR &
    ;;
  stop)
    kill $(ps aux | grep director-javafx*.jar | awk '{print $2}') >/dev/null
    ;;
esac