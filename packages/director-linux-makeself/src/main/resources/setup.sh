#!/bin/sh

# Trust Director install script
# Outline:
# 1. source the "functions.sh" file:  mtwilson-linux-util-3.0-SNAPSHOT.sh
# 2. load existing environment configuration
# 3. look for ~/director.env and source it if it's there
# 4. prompt for installation variables if they are not provided
# 5. determine if we are installing as root or non-root user; set paths
# 6. detect java
# 7. if java not installed, and we have it bundled, install it
# 8. unzip director archive director-zip-0.1-SNAPSHOT.zip into /opt/director, overwrite if any files already exist
# 9. link /usr/local/bin/director -> /opt/director/bin/director, if not already there
# 10. add director to startup services
# 11. look for DIRECTOR_PASSWORD environment variable; if not present print help message and exit:
#     Trust Director requires a master password
#     to generate a password run "export DIRECTOR_PASSWORD=$(director generate-password) && echo DIRECTOR_PASSWORD=$DIRECTOR_PASSWORD"
#     you must store this password in a safe place
#     losing the master password will result in data loss
# 12. director setup
# 13. director start

#####

# default settings
DIRECTOR_HOME=${DIRECTOR_HOME:-/opt/director}
DIRECTOR_LAYOUT=linux
DIRECTOR_PROPERTIES_FILE=${DIRECTOR_PROPERTIES_FILE:-"/opt/director/configuration/director.properties"}
INSTALL_LOG_DIRECTORY="/var/log/director"
INSTALL_LOG_FILE="$INSTALL_LOG_DIRECTORY/director_install.log"
mkdir -p "$INSTALL_LOG_DIRECTORY"

# functions script (mtwilson-linux-util-3.0-SNAPSHOT.sh) is required
# we use the following functions:
# java_detect java_ready_report 
# echo_failure echo_warning
# register_startup_script
UTIL_SCRIPT_FILE=$(ls -1 mtwilson-linux-util-*.sh | head -n 1)
if [ -f "$UTIL_SCRIPT_FILE" ]; then
  . $UTIL_SCRIPT_FILE
fi

DIRECTOR_UTIL_SCRIPT_FILE=$(ls -1 director-functions.sh | head -n 1)
if [ -f "$DIRECTOR_UTIL_SCRIPT_FILE" ]; then
  . $DIRECTOR_UTIL_SCRIPT_FILE
fi

# load existing environment first
load_director_conf
load_director_defaults

# environment file; override existing environment
if [ -f ~/director.env ]; then
  echo "Loading environment variables from $(cd ~ && pwd)/director.env"
  . ~/director.env
  env_file_exports=$(cat ~/director.env | grep -E '^[A-Z0-9_]+\s*=' | cut -d = -f 1)
  eval export $env_file_exports
else
  echo "No environment file"
fi

prompt_with_default USERNAME "Director Username:" "$USERNAME"
prompt_with_default_password PASSWORD "Director Password:" "$PASSWORD"
prompt_with_default TENANT_NAME "Tenant Name:" "$TENANT_NAME"
prompt_with_default MYSTERYHILL_KEY_NAME "Mystery Hill Key Name:" "$MYSTERYHILL_KEY_NAME"
prompt_with_default MYSTERYHILL_KEYSTORE "Mystery Hill Keystore:" "$MYSTERYHILL_KEYSTORE"
prompt_with_default_password MYSTERYHILL_KEYSTORE_PASSWORD "Mystery Hill Keystore Password:" "$MYSTERYHILL_KEYSTORE_PASSWORD"
prompt_with_default_password MYSTERYHILL_TLS_SSL_PASSWORD "Mystery Hill TLS Password:" "$MYSTERYHILL_TLS_SSL_PASSWORD"
prompt_with_default MTWILSON_USERNAME "Mtwilson Username:" "$MTWILSON_USERNAME"
prompt_with_default_password MTWILSON_PASSWORD "Mtwilson Password:" "$MTWILSON_PASSWORD"
prompt_with_default MTWILSON_SERVER_IP "Mtwilson Server IP:" "$MTWILSON_SERVER_IP"
prompt_with_default MTWILSON_SERVER_PORT "Mtwilson Server Port:" "$MTWILSON_SERVER_PORT"
prompt_with_default KMS_SERVER "Key Management Server:" "$KMS_SERVER"
prompt_with_default GLANCE_SERVER "Glance Server:" "$GLANCE_SERVER"
prompt_with_default HASH_TYPE "Hash Type:" "$HASH_TYPE"
prompt_with_default IMAGE_STORE_TYPE "Image Store Type:" "$IMAGE_STORE_TYPE"
prompt_with_default CUSTOMER_ID "Customer ID:" "$CUSTOMER_ID"

# determine if we are installing as root or non-root
if [ "$(whoami)" == "root" ]; then
  # create a director user if there isn't already one created
  DIRECTOR_USERNAME=${DIRECTOR_USERNAME:-director}
  if ! getent passwd $DIRECTOR_USERNAME 2>&1 >/dev/null; then
    useradd --comment "Mt Wilson Trust Director" --home $DIRECTOR_HOME --system --shell /bin/false $DIRECTOR_USERNAME
    usermod --lock $DIRECTOR_USERNAME
    # note: to assign a shell and allow login you can run "usermod --shell /bin/bash --unlock $DIRECTOR_USERNAME"
  fi
else
  # already running as director user
  DIRECTOR_USERNAME=$(whoami)
  echo_warning "Running as $DIRECTOR_USERNAME; if installation fails try again as root"
  if [ ! -w "$DIRECTOR_HOME" ] && [ ! -w $(dirname $DIRECTOR_HOME) ]; then
    export DIRECTOR_HOME=$(cd ~ && pwd)
  fi
fi

# if an existing director is already running, stop it while we install
if which director; then
  director stop
fi

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

# backup current configuration, if there is one
if [ -d $DIRECTOR_CONFIGURATION ]; then
  backup_conf_dir=$DIRECTOR_REPOSITORY/backup/configuration.$(date +"%Y%m%d.%H%M")
  mkdir -p $backup_conf_dir
  cp -R $DIRECTOR_CONFIGURATION/* $backup_conf_dir
fi

# create application directories (chown will be repeated near end of this script, after setup)
for directory in $DIRECTOR_HOME $DIRECTOR_CONFIGURATION $DIRECTOR_ENV $DIRECTOR_REPOSITORY $DIRECTOR_LOGS; do
  mkdir -p $directory
  chown -R $DIRECTOR_USERNAME:$DIRECTOR_USERNAME $directory
  chmod 700 $directory
done


# store directory layout in env file
echo "# $(date)" > $DIRECTOR_ENV/director-layout
echo "export DIRECTOR_HOME=$DIRECTOR_HOME" >> $DIRECTOR_ENV/director-layout
echo "export DIRECTOR_CONFIGURATION=$DIRECTOR_CONFIGURATION" >> $DIRECTOR_ENV/director-layout
echo "export DIRECTOR_REPOSITORY=$DIRECTOR_REPOSITORY" >> $DIRECTOR_ENV/director-layout
echo "export DIRECTOR_LOGS=$DIRECTOR_LOGS" >> $DIRECTOR_ENV/director-layout
echo "export DIRECTOR_ENV=$DIRECTOR_ENV" >> $DIRECTOR_ENV/director-layout

# store director username in env file
echo "# $(date)" > $DIRECTOR_ENV/director-username
echo "export DIRECTOR_USERNAME=$DIRECTOR_USERNAME" >> $DIRECTOR_ENV/director-username

# store the auto-exported environment variables in env file
# to make them available after the script uses sudo to switch users;
# we delete that file later
echo "# $(date)" > $DIRECTOR_ENV/director-setup
for env_file_var_name in $env_file_exports
do
  eval env_file_var_value="\$$env_file_var_name"
  echo "export $env_file_var_name=$env_file_var_value" >> $DIRECTOR_ENV/director-setup
done

# director requires java 1.7 or later
# detect or install java (jdk-1.7.0_51-linux-x64.tar.gz)
JAVA_REQUIRED_VERSION=${JAVA_REQUIRED_VERSION:-1.7}
java_detect
if ! java_ready; then
  # java not installed, check if we have the bundle
  JAVA_INSTALL_REQ_BUNDLE=$(ls -1 java-*.bin 2>/dev/null | head -n 1)
  if [ -n "$JAVA_INSTALL_REQ_BUNDLE" ]; then
    director_java_install
    java_detect
  fi
fi
if ! java_ready_report; then
  echo_failure "Java $JAVA_REQUIRED_VERSION not found"
  exit 1
fi

# make sure unzip and authbind are installed
DIRECTOR_YUM_PACKAGES="zip unzip authbind qemu-utils expect openssl sshfss kpartx vdfuse"
DIRECTOR_APT_PACKAGES="zip unzip authbind qemu-utils expect openssl sshfss kpartx vdfuse"
DIRECTOR_YAST_PACKAGES="zip unzip authbind qemu-utils expect openssl sshfss kpartx vdfuse"
DIRECTOR_ZYPPER_PACKAGES="zip unzip authbind qemu-utils expect openssl sshfss kpartx vdfuse"
auto_install "Installer requirements" "DIRECTOR"

# setup authbind to allow non-root director to listen on ports 80 and 443
if [ -n "$DIRECTOR_USERNAME" ] && [ "$DIRECTOR_USERNAME" != "root" ] && [ -d /etc/authbind/byport ]; then
  touch /etc/authbind/byport/80 /etc/authbind/byport/443
  chmod 500 /etc/authbind/byport/80 /etc/authbind/byport/443
  chown $DIRECTOR_USERNAME /etc/authbind/byport/80 /etc/authbind/byport/443
fi

# delete existing java files, to prevent a situation where the installer copies
# a newer file but the older file is also there
if [ -d $DIRECTOR_HOME/java ]; then
  rm $DIRECTOR_HOME/java/*.jar
fi

# extract director  (director-zip-0.1-SNAPSHOT.zip)
echo "Extracting application..."
DIRECTOR_ZIPFILE=`ls -1 director-*.zip 2>/dev/null | head -n 1`
unzip -oq $DIRECTOR_ZIPFILE -d $DIRECTOR_HOME

# copy utilities script file to application folder
cp $UTIL_SCRIPT_FILE $DIRECTOR_HOME/bin/functions.sh

# set permissions
chown -R $DIRECTOR_USERNAME:$DIRECTOR_USERNAME $DIRECTOR_HOME
chmod 755 $DIRECTOR_HOME/bin/*

# link /usr/local/bin/director -> /opt/director/bin/director
EXISTING_DIRECTOR_COMMAND=`which director`
if [ -z "$EXISTING_DIRECTOR_COMMAND" ]; then
  ln -s $DIRECTOR_HOME/bin/director.sh /usr/local/bin/director
fi


# register linux startup script
register_startup_script $DIRECTOR_HOME/bin/director.sh director

# the master password is required
if [ -z "$DIRECTOR_PASSWORD" ]; then
  echo_failure "Master password required in environment variable DIRECTOR_PASSWORD"
  echo 'To generate a new master password, run the following command:

  DIRECTOR_PASSWORD=$(director generate-password) && echo DIRECTOR_PASSWORD=$DIRECTOR_PASSWORD

The master password must be stored in a safe place, and it must
be exported in the environment for all other director commands to work.

LOSS OF MASTER PASSWORD WILL RESULT IN LOSS OF PROTECTED KEYS AND RELATED DATA

After you set DIRECTOR_PASSWORD, run the following command to complete installation:

  director setup

'
  exit 1
fi

# setup the director
director setup

# delete the temporary setup environment variables file
rm $DIRECTOR_ENV/director-setup

# ensure the director owns all the content created during setup
for directory in $DIRECTOR_HOME $DIRECTOR_CONFIGURATION $DIRECTOR_ENV $DIRECTOR_REPOSITORY $DIRECTOR_LOGS; do
  chown -R $DIRECTOR_USERNAME:$DIRECTOR_USERNAME $directory
done

# start the server
director start




