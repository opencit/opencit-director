#!/bin/bash

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
# note the layout setting is used only by this script
# and it is not saved or used by the app script
export DIRECTOR_HOME=${DIRECTOR_HOME:-/opt/director}
DIRECTOR_LAYOUT=${DIRECTOR_LAYOUT:-home}

# the env directory is not configurable; it is defined as DIRECTOR_HOME/env and
# the administrator may use a symlink if necessary to place it anywhere else
export DIRECTOR_ENV=$DIRECTOR_HOME/env

# load application environment variables if already defined
if [ -d $DIRECTOR_ENV ]; then
  DIRECTOR_ENV_FILES=$(ls -1 $DIRECTOR_ENV/*)
  for env_file in $DIRECTOR_ENV_FILES; do
    . $env_file
    env_file_exports=$(cat $env_file | grep -E '^[A-Z0-9_]+\s*=' | cut -d = -f 1)
    if [ -n "$env_file_exports" ]; then eval export $env_file_exports; fi
  done
fi

#Create upload dir and mount dir
if [ -d /mnt/director ]; then
  echo "Mount directory exists"
else
	mkdir /mnt/director	
fi

if [ -d /mnt/images ]; then
  echo "Upload directory exists"
else
	mkdir /mnt/images	
fi

# functions script (mtwilson-linux-util-3.0-SNAPSHOT.sh) is required
# we use the following functions:
# java_detect java_ready_report 
# echo_failure echo_warning
# register_startup_script
UTIL_SCRIPT_FILE=$(ls -1 mtwilson-linux-util-*.sh | head -n 1)
if [ -n "$UTIL_SCRIPT_FILE" ] && [ -f "$UTIL_SCRIPT_FILE" ]; then
  . $UTIL_SCRIPT_FILE
fi

DIRECTOR_UTIL_SCRIPT_FILE=$(ls -1 director-functions.sh | head -n 1)
if [ -n "$DIRECTOR_UTIL_SCRIPT_FILE" ] && [ -f "$DIRECTOR_UTIL_SCRIPT_FILE" ]; then
  . $DIRECTOR_UTIL_SCRIPT_FILE
fi

# load installer environment file, if present
if [ -f ~/director.env ]; then
  echo "Loading environment variables from $(cd ~ && pwd)/director.env"
  . ~/director.env
  env_file_exports=$(cat ~/director.env | grep -E '^[A-Z0-9_]+\s*=' | cut -d = -f 1)
  if [ -n "$env_file_exports" ]; then eval export $env_file_exports; fi
else
  echo "No environment file"
fi

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
export DIRECTOR_BIN=$DIRECTOR_HOME/bin
export DIRECTOR_JAVA=$DIRECTOR_HOME/java

# note that the env dir is not configurable; it is defined as "env" under home
export DIRECTOR_ENV=$DIRECTOR_HOME/env

director_backup_configuration() {
  if [ -n "$DIRECTOR_CONFIGURATION" ] && [ -d "$DIRECTOR_CONFIGURATION" ]; then
    datestr=`date +%Y%m%d.%H%M`
    backupdir=/var/backup/director.configuration.$datestr
    cp -r $DIRECTOR_CONFIGURATION $backupdir
  fi
}

director_backup_repository() {
  if [ -n "$DIRECTOR_REPOSITORY" ] && [ -d "$DIRECTOR_REPOSITORY" ]; then
    datestr=`date +%Y%m%d.%H%M`
    backupdir=/var/backup/director.repository.$datestr
    cp -r $DIRECTOR_REPOSITORY $backupdir
  fi
}

# backup current configuration and data, if they exist
director_backup_configuration
director_backup_repository

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
echo "export DIRECTOR_JAVA=$DIRECTOR_JAVA" >> $DIRECTOR_ENV/director-layout
echo "export DIRECTOR_BIN=$DIRECTOR_BIN" >> $DIRECTOR_ENV/director-layout
echo "export DIRECTOR_LOGS=$DIRECTOR_LOGS" >> $DIRECTOR_ENV/director-layout

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

DIRECTOR_PROPERTIES_FILE=${DIRECTOR_PROPERTIES_FILE:-"$DIRECTOR_CONFIGURATION/director.properties"}
touch "$DIRECTOR_PROPERTIES_FILE"
chown "$DIRECTOR_USERNAME":"$DIRECTOR_USERNAME" "$DIRECTOR_PROPERTIES_FILE"
chmod 600 "$DIRECTOR_PROPERTIES_FILE"

GLANCE_PROPERTIES_FILE=${DIRECTOR_PROPERTIES_FILE:-"$DIRECTOR_CONFIGURATION/glance.properties"}
touch "$GLANCE_PROPERTIES_FILE"
chown "$DIRECTOR_USERNAME":"$DIRECTOR_USERNAME" "$GLANCE_PROPERTIES_FILE"
chmod 600 "$GLANCE_PROPERTIES_FILE"

KMS_PROPERTIES_FILE=${KMS_PROPERTIES_FILE:-"$DIRECTOR_CONFIGURATION/kms.properties"}
touch "$KMS_PROPERTIES_FILE"
chown "$DIRECTOR_USERNAME":"$DIRECTOR_USERNAME" "$KMS_PROPERTIES_FILE"
chmod 600 "$KMS_PROPERTIES_FILE"

MTWILSON_PROPERTIES_FILE=${MTWILSON_PROPERTIES_FILE:-"$DIRECTOR_CONFIGURATION/mtwilson.properties"}
touch "$MTWILSON_PROPERTIES_FILE"
chown "$DIRECTOR_USERNAME":"$DIRECTOR_USERNAME" "$MTWILSON_PROPERTIES_FILE"
chmod 600 "$MTWILSON_PROPERTIES_FILE"

DIRECTOR_INSTALL_LOG_FILE=${DIRECTOR_INSTALL_LOG_FILE:-"$DIRECTOR_LOGS/director_install.log"}
export INSTALL_LOG_FILE="$DIRECTOR_INSTALL_LOG_FILE"
touch "$DIRECTOR_INSTALL_LOG_FILE"
chown "$DIRECTOR_USERNAME":"$DIRECTOR_USERNAME" "$DIRECTOR_INSTALL_LOG_FILE"
chmod 600 "$DIRECTOR_INSTALL_LOG_FILE"

# load existing environment; set variables will take precendence
load_director_conf
load_director_defaults

#prompt_with_default MYSTERYHILL_KEY_NAME "Mystery Hill Key Name:" "$MYSTERYHILL_KEY_NAME"
#prompt_with_default MYSTERYHILL_KEYSTORE "Mystery Hill Keystore:" "$MYSTERYHILL_KEYSTORE"
#prompt_with_default_password MYSTERYHILL_KEYSTORE_PASSWORD "Mystery Hill Keystore Password:" "$MYSTERYHILL_KEYSTORE_PASSWORD"
#prompt_with_default_password MYSTERYHILL_TLS_SSL_PASSWORD "Mystery Hill TLS Password:" "$MYSTERYHILL_TLS_SSL_PASSWORD"
#prompt_with_default KMS_SERVER "Key Management Server:" "$KMS_SERVER"

# required TD properties
prompt_with_default DIRECTOR_ID "Trust Director ID:" "$DIRECTOR_ID"
update_property_in_file "director.id" "$DIRECTOR_PROPERTIES_FILE" "$DIRECTOR_ID"
#prompt_with_default VM_WHITELIST_HASH_TYPE "Specify the hash type algorithm to use during VM whitelist:" "$VM_WHITELIST_HASH_TYPE"
update_property_in_file "vm.whitelist.hash.type" "$DIRECTOR_PROPERTIES_FILE" "$VM_WHITELIST_HASH_TYPE"
prompt_with_default IMAGE_STORE_TYPE "Image Store Type:" "$IMAGE_STORE_TYPE"
update_property_in_file "image.store.type" "$DIRECTOR_PROPERTIES_FILE" "$IMAGE_STORE_TYPE"
if [ $IMAGE_STORE_TYPE != "Openstack_Glance" ]; then
	echo_failure "Image store type $IMAGE_STORE_TYPE is not supported. Supported type is: Openstack_Glance"
fi
prompt_with_default IMAGE_STORE_SERVER "Image Store Server:" "$IMAGE_STORE_SERVER"
update_property_in_file "image.store.server" "$DIRECTOR_PROPERTIES_FILE" "$IMAGE_STORE_SERVER"
prompt_with_default IMAGE_STORE_USERNAME "Image Store Username:" "$IMAGE_STORE_USERNAME"
update_property_in_file "image.store.username" "$DIRECTOR_PROPERTIES_FILE" "$IMAGE_STORE_USERNAME"
prompt_with_default_password IMAGE_STORE_PASSWORD "Image Store Password:" "$IMAGE_STORE_PASSWORD"
update_property_in_file "image.store.password" "$DIRECTOR_PROPERTIES_FILE" "$IMAGE_STORE_PASSWORD"
prompt_with_default TENANT_NAME "Tenant Name:" "$TENANT_NAME"
update_property_in_file "tenant.name" "$DIRECTOR_PROPERTIES_FILE" "$TENANT_NAME"
#validating image store credentials
if [ $IMAGE_STORE_TYPE == "Openstack_Glance" ]; then
	http_status_code=`curl -i -d '{"auth": {"tenantName": "'$TENANT_NAME'", "passwordCredentials": {"username": "'$IMAGE_STORE_USERNAME'", "password": "'$IMAGE_STORE_PASSWORD'"}}}'  -H "Content-type: application/json" http://$IMAGE_STORE_SERVER:5000/v2.0/tokens 2>/dev/null | head -n 1 | cut -d$' ' -f2`
	if [ $http_status_code == "200" ]; then
			echo "$IMAGE_STORE_TYPE credentials are validated successfully"
	else
			echo_failure "Can not connect to $IMAGE_STORE_TYPE using given credentials"
	fi
fi

#required glance.properties
update_property_in_file "glance.ip" "$GLANCE_PROPERTIES_FILE" "$GLANCE_IMAGE_STORE_IP"
update_property_in_file "glance.port" "$GLANCE_PROPERTIES_FILE" "$GLANCE_IMAGE_STORE_PORT"
update_property_in_file "glance.image.store.username" "$GLANCE_PROPERTIES_FILE" "$GLANCE_IMAGE_STORE_USERNAME"
update_property_in_file "glance.image.store.password" "$GLANCE_PROPERTIES_FILE" "$GLANCE_IMAGE_STORE_PASSWORD"
update_property_in_file "glance.tenant.name" "$GLANCE_PROPERTIES_FILE" "$GLANCE_TENANT_NAME"

# modifying after mtwilson api client built
prompt_with_default MTWILSON_SERVER "Mtwilson Server:" "$MTWILSON_SERVER"
update_property_in_file "mtwilson.server" "$DIRECTOR_PROPERTIES_FILE" "$MTWILSON_SERVER"
prompt_with_default MTWILSON_SERVER_PORT "Mtwilson Server Port:" "$MTWILSON_SERVER_PORT"
update_property_in_file "mtwilson.server.port" "$DIRECTOR_PROPERTIES_FILE" "$MTWILSON_SERVER_PORT"
prompt_with_default MTWILSON_USERNAME "Mtwilson Username:" "$MTWILSON_USERNAME"
update_property_in_file "mtwilson.username" "$DIRECTOR_PROPERTIES_FILE" "$MTWILSON_USERNAME"
prompt_with_default_password MTWILSON_PASSWORD "Mtwilson Password:" "$MTWILSON_PASSWORD"
update_property_in_file "mtwilson.password" "$DIRECTOR_PROPERTIES_FILE" "$MTWILSON_PASSWORD"


#############################################
#update for TDAAS: Write 2 different files for KMS andd MtWilson settings
#############################################
#MtWilson
update_property_in_file "mtwilson.server" "$MTWILSON_PROPERTIES_FILE" "$MTWILSON_SERVER"
update_property_in_file "mtwilson.server.port" "$MTWILSON_PROPERTIES_FILE" "$MTWILSON_SERVER_PORT"
update_property_in_file "mtwilson.username" "$MTWILSON_PROPERTIES_FILE" "$MTWILSON_USERNAME"
update_property_in_file "mtwilson.password" "$MTWILSON_PROPERTIES_FILE" "$MTWILSON_PASSWORD"

#validating MTW credentials 
http_status_code=`curl --insecure -i -X GET "https://$MTWILSON_SERVER:$MTWILSON_SERVER_PORT/mtwilson/v2/hosts?nameEqualTo=nameEw" -u $MTWILSON_USERNAME:$MTWILSON_PASSWORD 2>/dev/null | head -n 1 | cut -d$' ' -f2`
if [ $http_status_code == "200" ] || [ $http_status_code == "500" ]; then
        echo "MtWilson credentials are validated successfully"
else
        echo_failure "Can not connect to MtWilson using given credentials"
fi

# director requires java 1.7 or later
# detect or install java (jdk-1.7.0_51-linux-x64.tar.gz)
JAVA_REQUIRED_VERSION=${JAVA_REQUIRED_VERSION:-1.7}
java_detect
if ! java_ready; then
  # java not installed, check if we have the bundle
  JAVA_INSTALL_REQ_BUNDLE=$(ls -1 jdk-*.tar.gz 2>/dev/null | head -n 1)
  if [ -n "$JAVA_INSTALL_REQ_BUNDLE" ]; then
    director_java_install
    java_detect
  fi
fi
if java_ready_report; then
  echo "# $(date)" > $DIRECTOR_ENV/director-java
  echo "export JAVA_HOME=$JAVA_HOME" >> $DIRECTOR_ENV/director-java
  echo "export JAVA_CMD=$java" >> $DIRECTOR_ENV/director-java
else
  echo_failure "Java $JAVA_REQUIRED_VERSION not found"
  exit 1
fi

# make sure unzip and authbind are installed
DIRECTOR_YUM_PACKAGES="zip unzip authbind qemu-utils expect openssl sshfs kpartx vdfuse"
DIRECTOR_APT_PACKAGES="zip unzip authbind qemu-utils expect openssl sshfs kpartx " #vdfuse"
DIRECTOR_YAST_PACKAGES="zip unzip authbind qemu-utils expect openssl sshfs kpartx vdfuse"
DIRECTOR_ZYPPER_PACKAGES="zip unzip authbind qemu-utils expect openssl sshfs kpartx vdfuse"
auto_install "Installer requirements" "DIRECTOR"
if [ $? -ne 0 ]; then echo_failure "Failed to install prerequisites through package installer"; exit -1; fi

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

# setup the director, unless the NOSETUP variable is defined
if [ -z "$DIRECTOR_NOSETUP" ]; then
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

  director config mtwilson.extensions.fileIncludeFilter.contains "${MTWILSON_EXTENSIONS_FILEINCLUDEFILTER_CONTAINS:-mtwilson,director}" >/dev/null
  director setup
fi

# delete the temporary setup environment variables file
rm -f $DIRECTOR_ENV/director-setup

# ensure the director owns all the content created during setup
for directory in $DIRECTOR_HOME $DIRECTOR_CONFIGURATION $DIRECTOR_JAVA $DIRECTOR_BIN $DIRECTOR_ENV $DIRECTOR_REPOSITORY $DIRECTOR_LOGS; do
  chown -R $DIRECTOR_USERNAME:$DIRECTOR_USERNAME $directory
done

# start the server, unless the NOSETUP variable is defined
#if [ -z "$DIRECTOR_NOSETUP" ]; then director start; fi
echo_success "Installation complete"
