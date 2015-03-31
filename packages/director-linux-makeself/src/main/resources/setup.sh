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
UTIL_SCRIPT_FILE=`ls -1 mtwilson-linux-util-*.sh | head -n 1`
if [ -f "$UTIL_SCRIPT_FILE" ]; then
  . $UTIL_SCRIPT_FILE
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
prompt_with_default KMS_SERVER_IP "Key Management Server IP:" "$KMS_SERVER_IP"
prompt_with_default GLANCE_IP "Glance IP:" "$GLANCE_IP"
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




########## FUNCTIONS LIBRARY ##########
load_director_conf() {
  DIRECTOR_PROPERTIES_FILE=${DIRECTOR_PROPERTIES_FILE:-"/opt/director/configuration/director.properties"}
  if [ -n "$DEFAULT_ENV_LOADED" ]; then return; fi

  # director.properties file
  if [ -f "$DIRECTOR_PROPERTIES_FILE" ]; then
    echo -n "Reading properties from file [$DIRECTOR_PROPERTIES_FILE]....."
    export CONF_USERNAME=$(read_property_from_file "username" "$DIRECTOR_PROPERTIES_FILE")
    export CONF_PASSWORD=$(read_property_from_file "password" "$DIRECTOR_PROPERTIES_FILE")
    export CONF_TENANT_NAME=$(read_property_from_file "tenant.name" "$DIRECTOR_PROPERTIES_FILE")
    export CONF_MYSTERYHILL_KEY_NAME=$(read_property_from_file "mysteryhill.key.name" "$DIRECTOR_PROPERTIES_FILE")
    export CONF_MYSTERYHILL_KEYSTORE=$(read_property_from_file "mysteryhill.keystore" "$DIRECTOR_PROPERTIES_FILE")
    export CONF_MYSTERYHILL_KEYSTORE_PASSWORD=$(read_property_from_file "mysteryhill.keystore.password" "$DIRECTOR_PROPERTIES_FILE")
    export CONF_MYSTERYHILL_TLS_SSL_PASSWORD=$(read_property_from_file "mysteryhill.tls.ssl.password" "$DIRECTOR_PROPERTIES_FILE")
    export CONF_MTWILSON_USERNAME=$(read_property_from_file "mtwilson.username" "$DIRECTOR_PROPERTIES_FILE")
    export CONF_MTWILSON_PASSWORD=$(read_property_from_file "mtwilson.password" "$DIRECTOR_PROPERTIES_FILE")
    export CONF_MTWILSON_SERVER_IP=$(read_property_from_file "mtwilson.server.ip" "$DIRECTOR_PROPERTIES_FILE")
    export CONF_MTWILSON_SERVER_PORT=$(read_property_from_file "mtwilson.server.port" "$DIRECTOR_PROPERTIES_FILE")
    export CONF_KMS_SERVER_IP=$(read_property_from_file "kms.server.ip" "$DIRECTOR_PROPERTIES_FILE")
    export CONF_GLANCE_IP=$(read_property_from_file "glance.ip" "$DIRECTOR_PROPERTIES_FILE")
    export CONF_HASH_TYPE=$(read_property_from_file "hash.type" "$DIRECTOR_PROPERTIES_FILE")
    export CONF_IMAGE_STORE_TYPE=$(read_property_from_file "image.store.type" "$DIRECTOR_PROPERTIES_FILE")
	export CONF_CUSTOMER_ID=$(read_property_from_file "customer.id" "$DIRECTOR_PROPERTIES_FILE")
    echo_success "Done"
  fi

  export DEFAULT_ENV_LOADED=true
  return 0
}

load_director_defaults() {
  export DEFAULT_USERNAME=""
  export DEFAULT_PASSWORD=""
  export DEFAULT_TENANT_NAME=""
  export DEFAULT_MYSTERYHILL_KEY_NAME=""
  export DEFAULT_MYSTERYHILL_KEYSTORE=""
  export DEFAULT_MYSTERYHILL_KEYSTORE_PASSWORD=""
  export DEFAULT_MYSTERYHILL_TLS_SSL_PASSWORD=""
  export DEFAULT_MTWILSON_USERNAME=""
  export DEFAULT_MTWILSON_PASSWORD=""
  export DEFAULT_MTWILSON_SERVER_IP=""
  export DEFAULT_MTWILSON_SERVER_PORT=""
  export DEFAULT_KMS_SERVER_IP=""
  export DEFAULT_GLANCE_IP=""
  export DEFAULT_HASH_TYPE=""
  export DEFAULT_IMAGE_STORE_TYPE=""
  export DEFAULT_CUSTOMER_ID=""

  export USERNAME=${USERNAME:-${CONF_USERNAME:-$DEFAULT_USERNAME}}
  export PASSWORD=${PASSWORD:-${CONF_PASSWORD:-$DEFAULT_PASSWORD}}
  export TENANT_NAME=${TENANT_NAME:-${CONF_TENANT_NAME:-$DEFAULT_TENANT_NAME}}
  export MYSTERYHILL_KEY_NAME=${MYSTERYHILL_KEY_NAME:-${CONF_MYSTERYHILL_KEY_NAME:-$DEFAULT_MYSTERYHILL_KEY_NAME}}
  export MYSTERYHILL_KEYSTORE=${MYSTERYHILL_KEYSTORE:-${CONF_MYSTERYHILL_KEYSTORE:-$DEFAULT_MYSTERYHILL_KEYSTORE}}
  export MYSTERYHILL_KEYSTORE_PASSWORD=${MYSTERYHILL_KEYSTORE_PASSWORD:-${CONF_MYSTERYHILL_KEYSTORE_PASSWORD:-$DEFAULT_MYSTERYHILL_KEYSTORE_PASSWORD}}
  export MYSTERYHILL_TLS_SSL_PASSWORD=${MYSTERYHILL_TLS_SSL_PASSWORD:-${CONF_MYSTERYHILL_TLS_SSL_PASSWORD:-$DEFAULT_MYSTERYHILL_TLS_SSL_PASSWORD}}
  export MTWILSON_USERNAME=${MTWILSON_USERNAME:-${CONF_MTWILSON_USERNAME:-$DEFAULT_MTWILSON_USERNAME}}
  export MTWILSON_PASSWORD=${MTWILSON_PASSWORD:-${CONF_MTWILSON_PASSWORD:-$DEFAULT_MTWILSON_PASSWORD}}
  export MTWILSON_SERVER_IP=${MTWILSON_SERVER_IP:-${CONF_MTWILSON_SERVER_IP:-$DEFAULT_MTWILSON_SERVER_IP}}
  export MTWILSON_SERVER_PORT=${MTWILSON_SERVER_PORT:-${CONF_MTWILSON_SERVER_PORT:-$DEFAULT_MTWILSON_SERVER_PORT}}
  export KMS_SERVER_IP=${KMS_SERVER_IP:-${CONF_KMS_SERVER_IP:-$DEFAULT_KMS_SERVER_IP}}
  export GLANCE_IP=${GLANCE_IP:-${CONF_GLANCE_IP:-$DEFAULT_GLANCE_IP}}
  export HASH_TYPE=${HASH_TYPE:-${CONF_HASH_TYPE:-$DEFAULT_HASH_TYPE}}
  export IMAGE_STORE_TYPE=${IMAGE_STORE_TYPE:-${CONF_IMAGE_STORE_TYPE:-$DEFAULT_IMAGE_STORE_TYPE}}
  export CUSTOMER_ID=${CUSTOMER_ID:-${CONF_CUSTOMER_ID:-$DEFAULT_CUSTOMER_ID}}
}

director_java_install() {
  java_clear; java_detect 2>&1 >> $INSTALL_LOG_FILE
  JAVA_PACKAGE=$(ls -1d jdk*)
  if [[ -z "$JAVA_PACKAGE" || ! -f "$JAVA_PACKAGE" ]]; then
    echo_failure "Missing Java installer: $JAVA_PACKAGE" | tee -a 
    return 1
  fi
  javafile=$JAVA_PACKAGE
  echo "Installing $javafile" >> $INSTALL_LOG_FILE
  is_targz=$(echo $javafile | grep -E ".tar.gz$|.tgz$")
  is_gzip=$(echo $javafile | grep ".gz$")
  is_bin=$(echo $javafile | grep ".bin$")
  javaname=$(echo $javafile | awk -F . '{ print $1 }')
  if [ -n "$is_targz" ]; then
    tar xzvf $javafile 2>&1 >> $INSTALL_LOG_FILE
  elif [ -n "$is_gzip" ]; then
    gunzip $javafile 2>&1 >/dev/null >> $INSTALL_LOG_FILE
    chmod +x $javaname
    ./$javaname | grep -vE "inflating:|creating:|extracting:|linking:|^Creating" 
  elif [ -n "$is_bin" ]; then
    chmod +x $javafile
    ./$javafile | grep -vE "inflating:|creating:|extracting:|linking:|^Creating"  
  fi
  # java gets unpacked in current directory but they cleverly
  # named the folder differently than the archive, so search for it:
  java_unpacked=$(ls -d */ 2>/dev/null)
  for f in $java_unpacked
  do
    if [ -d "/usr/lib/jvm/$f" ]; then
      echo "Java already installed at /usr/lib/jvm/$f"
      export JAVA_HOME="/usr/lib/jvm/$f"
    else
      echo "Installing Java..."
      mkdir -p "/usr/lib/jvm"
      mv "$f" "/usr/lib/jvm"
      export JAVA_HOME="/usr/lib/jvm/$f"
      #update-alternatives --install "/usr/bin/java" "java" "/usr/lib/jvm/$f/bin/java" 1
      #update-alternatives --install "/usr/bin/javac" "javac" "/usr/lib/jvm/$f/bin/javac" 1
      #update-alternatives --install "/usr/bin/javaws" "javaws" "/usr/lib/jvm/$f/bin/javaws" 1
      ##update-alternatives --config java
      ##select /usr/lib/jvm/jdk1.7.0_55/bin/java
    fi
  done

  rm "/usr/bin/java" 2>/dev/null
  rm "/usr/bin/keytool" 2>/dev/null
  ln -s "$JAVA_HOME/jre/bin/java" "/usr/bin/java"
  ln -s "$JAVA_HOME/jre/bin/keytool" "/usr/bin/keytool"

  java_detect 2>&1 >> $INSTALL_LOG_FILE
  if [[ -z "$JAVA_HOME" || -z "$java" ]]; then
    echo_failure "Unable to auto-install Java" | tee -a $INSTALL_LOG_FILE
    echo "  Java download URL:"                >> $INSTALL_LOG_FILE
    echo "  http://www.java.com/en/download/"  >> $INSTALL_LOG_FILE
  fi
}