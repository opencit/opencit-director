#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

#currentUser=`whoami`
#if [ ! $currentUser == "root" ]; then
#  echo_failure "You must be root user to install Mtwilson Trust Director."
#  exit -1
#fi

#load the functions file first so we can use the generatePassword function
if [ -f functions ]; then . functions; else echo "Missing file: functions"; exit 1; fi
if [ -f director.env ]; then  . director.env; fi

DIRECTOR_PROPERTIES_FILE="/opt/trustdirector/configuration/director.properties"
#handle encryption
#load_director_conf
#load_director_defaults

export DIRECTOR_OWNER=${DIRECTOR_OWNER:-director}
getent passwd $MTWILSON_OWNER >/dev/null
if [ $? != 0 ]; then
  echo "Creating Director owner account [$DIRECTOR_OWNER]"
  useradd -s /bin/false -d /opt/trustdirector $DIRECTOR_OWNER
  if [ $? != 0 ]; then
    echo_warning "Failed to create user '$DIRECTOR_OWNER'"
  fi
fi

DIRECTOR_APT_PACKAGES=""
DIRECTOR_YAST_PACKAGES=""
DIRECTOR_YUM_PACKAGES=""
DIRECTOR_ZYPPER_PACKAGES=""
auto_install "Installer requirements" "DIRECTOR"

