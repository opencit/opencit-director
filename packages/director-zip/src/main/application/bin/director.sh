#!/bin/bash

# chkconfig: 2345 80 30
# description: Intel Trust Director

### BEGIN INIT INFO
# Provides:          director
# Required-Start:    $remote_fs $syslog
# Required-Stop:     $remote_fs $syslog
# Should-Start:      $portmap
# Should-Stop:       $portmap
# X-Start-Before:    nis
# X-Stop-After:      nis
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# X-Interactive:     true
# Short-Description: director
# Description:       Main script to run director commands
### END INIT INFO
DESC="DIRECTOR"
NAME=director

###################################################################################################

# directory layout
export DIRECTOR_HOME=${DIRECTOR_HOME:-/opt/director}
export DIRECTOR_LAYOUT=linux

# define application directory layout
if [ "$DIRECTOR_LAYOUT" == "linux" ]; then
  export DIRECTOR_CONFIGURATION=${DIRECTOR_CONFIGURATION:-/etc/director}
  export DIRECTOR_REPOSITORY=${DIRECTOR_REPOSITORY:-/var/opt/director}
  export DIRECTOR_LOGS=${DIRECTOR_LOGS:-/var/log/director}
elif [ "$DIRECTOR_LAYOUT" == "home" ]; then
  export DIRECTOR_CONFIGURATION=${DIRECTOR_CONFIGURATION:-$DIRECTOR_HOME/configuration}
  export DIRECTOR_REPOSITORY=${DIRECTOR_REPOSITORY:-$DIRECTOR_HOME/repository}
  export DIRECTOR_LOGS=${DIRECTOR_LOGS:-$DIRECTOR_HOME/logs}
fi
export DIRECTOR_ENV=$DIRECTOR_CONFIGURATION/env
export DIRECTOR_JAVA=${DIRECTOR_JAVA:-$DIRECTOR_HOME/java}
export DIRECTOR_BIN=${DIRECTOR_BIN:-$DIRECTOR_HOME/bin}

#export DIRECTOR_CONFIGURATION=${DIRECTOR_CONFIGURATION:-${DIRECTOR_CONF:-$DIRECTOR_HOME/configuration}}
#export DIRECTOR_ENV=${DIRECTOR_ENV:-$DIRECTOR_CONFIGURATION/env}
#export DIRECTOR_JAVA=${DIRECTOR_JAVA:-$DIRECTOR_HOME/java}
#export DIRECTOR_BIN=${DIRECTOR_BIN:-$DIRECTOR_HOME/bin}
#export DIRECTOR_REPOSITORY=${DIRECTOR_REPOSITORY:-$DIRECTOR_HOME/repository}
#export DIRECTOR_LOGS=${DIRECTOR_LOGS:-$DIRECTOR_HOME/logs}

###################################################################################################

# load environment variables (these may override the defaults set above)
if [ -d $DIRECTOR_ENV ]; then
  DIRECTOR_ENV_FILES=$(ls -1 $DIRECTOR_ENV/*)
  for env_file in $DIRECTOR_ENV_FILES; do
    . $env_file
  done
fi

# if non-root execution is specified, and we are currently root, start over; the DIRECTOR_SUDO variable limits this to one attempt
# we make an exception for the uninstall command, which may require root access to delete users and certain directories
if [ -n "$DIRECTOR_USERNAME" ] && [ "$DIRECTOR_USERNAME" != "root" ] && [ $(whoami) == "root" ] && [ -z "$DIRECTOR_SUDO" ] && [ "$1" != "uninstall" ]; then
  sudo -u $DIRECTOR_USERNAME DIRECTOR_PASSWORD=$DIRECTOR_PASSWORD DIRECTOR_SUDO=true director $*
  exit $?
fi

# load linux utility
if [ -f "$DIRECTOR_HOME/bin/functions.sh" ]; then
  . $DIRECTOR_HOME/bin/functions.sh
fi

###################################################################################################

# all other variables with defaults
DIRECTOR_APPLICATION_LOG_FILE=${DIRECTOR_APPLICATION_LOG_FILE:-$DIRECTOR_LOGS/director.log}
JAVA_REQUIRED_VERSION=${JAVA_REQUIRED_VERSION:-1.7}
JAVA_OPTS=${JAVA_OPTS:-"-Dlogback.configurationFile=$DIRECTOR_CONFIGURATION/logback.xml"}

DIRECTOR_SETUP_FIRST_TASKS=${DIRECTOR_SETUP_FIRST_TASKS:-"update-extensions-cache-file"}
DIRECTOR_SETUP_TASKS=${DIRECTOR_SETUP_TASKS:-"password-vault"}

# the standard PID file location /var/run is typically owned by root;
# if we are running as non-root and the standard location isn't writable 
# then we need a different place
DIRECTOR_PID_FILE=${DIRECTOR_PID_FILE:-/var/run/director.pid}
if [ ! -w "$DIRECTOR_PID_FILE" ] && [ ! -w $(dirname "$DIRECTOR_PID_FILE") ]; then
  DIRECTOR_PID_FILE=$DIRECTOR_REPOSITORY/director.pid
fi

###################################################################################################

# generated variables
JARS=$(ls -1 $DIRECTOR_JAVA/*.jar)
CLASSPATH=$(echo $JARS | tr ' ' ':')

# the classpath is long and if we use the java -cp option we will not be
# able to see the full command line in ps because the output is normally
# truncated at 4096 characters. so we export the classpath to the environment
export CLASSPATH

###################################################################################################

# run a director command
director_run() {
  local args="$*"
  java $JAVA_OPTS com.intel.mtwilson.launcher.console.Main $args
  return $?
}

# run default set of setup tasks and check if admin user needs to be created
director_complete_setup() {
  # run all setup tasks, don't use the force option to avoid clobbering existing
  # useful configuration files
  director_run setup $DIRECTOR_SETUP_FIRST_TASKS
  director_run setup $DIRECTOR_SETUP_TASKS
}

# arguments are optional, if provided they are the names of the tasks to run, in order
director_setup() {
  local args="$*"
  java $JAVA_OPTS com.intel.mtwilson.launcher.console.Main setup $args
  return $?
}

director_start() {
    if [ -z "$DIRECTOR_PASSWORD" ]; then
      echo_failure "Master password is required; export DIRECTOR_PASSWORD"
      return 1
    fi

    # check if we need to use authbind or if we can start java directly
    prog="java"
    if [ -n "$DIRECTOR_USERNAME" ] && [ "$DIRECTOR_USERNAME" != "root" ] && [ $(whoami) != "root" ] && [ -n $(which authbind) ]; then
      prog="authbind java"
      JAVA_OPTS="$JAVA_OPTS -Djava.net.preferIPv4Stack=true"
    fi

    # the subshell allows the java process to have a reasonable current working
    # directory without affecting the user's working directory. 
    # the last background process pid $! must be stored from the subshell.
    (
      cd $DIRECTOR_HOME
      $prog $JAVA_OPTS com.intel.mtwilson.launcher.console.Main start >>$DIRECTOR_APPLICATION_LOG_FILE 2>&1 &
      echo $! > $DIRECTOR_PID_FILE
    )
    if director_is_running; then
      echo_success "Started Trust Director"
    else
      echo_failure "Failed to start Trust Director"
    fi
}

# returns 0 if Trust Director is running, 1 if not running
# side effects: sets DIRECTOR_PID if Trust Director is running, or to empty otherwise
director_is_running() {
  DIRECTOR_PID=
  if [ -f $DIRECTOR_PID_FILE ]; then
    DIRECTOR_PID=$(cat $DIRECTOR_PID_FILE)
    local is_running=`ps -eo pid | grep "^\s*${DIRECTOR_PID}$"`
    if [ -z "$is_running" ]; then
      # stale PID file
      DIRECTOR_PID=
    fi
  fi
  if [ -z "$DIRECTOR_PID" ]; then
    # check the process list just in case the pid file is stale
    DIRECTOR_PID=$(ps ww | grep -v grep | grep java | grep "com.intel.mtwilson.launcher.console.Main start" | awk '{ print $1 }')
  fi
  if [ -z "$DIRECTOR_PID" ]; then
    # Trust Director is not running
    return 1
  fi
  # Trust Director is running and DIRECTOR_PID is set
  return 0
}


director_stop() {
  if director_is_running; then
    kill -9 $DIRECTOR_PID
    if [ $? ]; then
      echo "Stopped Trust Director"
      rm $DIRECTOR_PID_FILE
    else
      echo "Failed to stop Trust Director"
    fi
  fi
}

director_backup_configuration() {
    datestr=`date +%Y-%m-%d.%H%M`
    mkdir -p /var/backup/director.configuration.$datestr
    cp -r /opt/director/configuration/* /var/backup/director.configuration.$datestr
}

director_backup_repository() {
    datestr=`date +%Y-%m-%d.%H%M`
    mkdir -p /var/backup/director.repository.$datestr
    cp -r /opt/director/repository/* /var/backup/director.repository.$datestr
}

# removes Trust Director home directory (including configuration and data if they are there).
# if you need to keep those, back them up before calling uninstall,
# or if the configuration and data are outside the home directory
# they will not be removed, so you could configure DIRECTOR_CONFIGURATION=/etc/director
# and DIRECTOR_REPOSITORY=/var/opt/director and then they would not be deleted by this.
director_uninstall() {
    remove_startup_script director
	rm /usr/local/bin/director
    rm -rf /opt/director
    groupdel director > /dev/null 2>&1
    userdel director > /dev/null 2>&1
}

print_help() {
    echo "Usage: $0 start|stop|uninstall|version"
    echo "Usage: $0 setup [--force|--noexec] [task1 task2 ...]"
    echo "Available setup tasks:"
    echo $DIRECTOR_SETUP_TASKS | tr ' ' '\n'
}

###################################################################################################

# here we look for specific commands first that we will handle in the
# script, and anything else we send to the java application

case "$1" in
  help)
    print_help
    ;;
  start)
    director_start
    ;;
  stop)
    director_stop
    ;;
  restart)
    director_stop
    director_start
    ;;
  status)
    if director_is_running; then
      echo "Trust Director is running"
      exit 0
    else
      echo "Trust Director is not running"
      exit 1
    fi
    ;;
  setup)
    shift
    if [ -n "$1" ]; then
      director_setup $*
    else
      director_complete_setup
    fi
    ;;
  uninstall)
    director_stop
    director_uninstall
    ;;
  *)
    if [ -z "$*" ]; then
      print_help
    else
      #echo "args: $*"
      java $JAVA_OPTS com.intel.mtwilson.launcher.console.Main $*
    fi
    ;;
esac


exit $?
