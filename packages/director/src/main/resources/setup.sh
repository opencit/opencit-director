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

echo "install log file is" $INSTALL_LOG_FILE

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
update_property_in_file "tenant.name" "$DIRECTOR_PROPERTIES_FILE" "$TENANT_NAME"



#------------------ Glance properties

update_property_in_file "glance.ip" "$DIRECTOR_PROPERTIES_FILE" "$GLANCE_IMAGE_STORE_IP"
update_property_in_file "glance.port" "$DIRECTOR_PROPERTIES_FILE" "$GLANCE_IMAGE_STORE_PORT"
update_property_in_file "glance.image.store.username" "$DIRECTOR_PROPERTIES_FILE" "$GLANCE_IMAGE_STORE_USERNAME"
update_property_in_file "glance.image.store.password" "$DIRECTOR_PROPERTIES_FILE" "$GLANCE_IMAGE_STORE_PASSWORD"
update_property_in_file "glance.tenant.name" "$DIRECTOR_PROPERTIES_FILE" "$TENANT_NAME"



#-------------------


#required database properties

prompt_with_default DIRECTOR_DB_NAME "Drector db name:" "$DIRECTOR_DB_NAME"
update_property_in_file "director.db.name" "$DIRECTOR_PROPERTIES_FILE" "$DIRECTOR_DB_NAME"
prompt_with_default DIRECTOR_DB_HOSTNAME "Drector db Hostname:" "$DIRECTOR_DB_HOSTNAME"
update_property_in_file "director.db.hostname" "$DIRECTOR_PROPERTIES_FILE" "$DIRECTOR_DB_HOSTNAME"
prompt_with_default DIRECTOR_DB_PORTNUM "Drector db Portno:" "$DIRECTOR_DB_PORTNUM"
update_property_in_file "director.db.portnum" "$DIRECTOR_PROPERTIES_FILE" "$DIRECTOR_DB_PORTNUM"
prompt_with_default DIRECTOR_DB_USERNAME "Director db username:" "$DIRECTOR_DB_USERNAME"
update_property_in_file "director.db.username" "$DIRECTOR_PROPERTIES_FILE" "$DIRECTOR_DB_USERNAME"
prompt_with_default DIRECTOR_DB_PASSWORD "Director db password:" "$DIRECTOR_DB_PASSWORD"
update_property_in_file "director.db.password" "$DIRECTOR_PROPERTIES_FILE" "$DIRECTOR_DB_PASSWORD"
prompt_with_default DIRECTOR_DB_DRIVER "Director db driver:" "$DIRECTOR_DB_DRIVER"
update_property_in_file "director.db.driver" "$DIRECTOR_PROPERTIES_FILE" "$DIRECTOR_DB_DRIVER"

export DIRECTOR_DB_URL="jdbc:postgresql://${DIRECTOR_DB_HOSTNAME}:${DIRECTOR_DB_PORTNUM}/${DIRECTOR_DB_NAME}"
update_property_in_file "director.db.url" "$DIRECTOR_PROPERTIES_FILE" "$DIRECTOR_DB_URL"

export POSTGRES_HOSTNAME=${DIRECTOR_DB_HOSTNAME}
export POSTGRES_PORTNUM=${DIRECTOR_DB_PORTNUM}
export POSTGRES_DATABASE=${DIRECTOR_DB_NAME}
export POSTGRES_USERNAME=${DIRECTOR_DB_USERNAME}
export POSTGRES_PASSWORD=${DIRECTOR_DB_PASSWORD}


# modifying after mtwilson api client built
prompt_with_default MTWILSON_SERVER "Mtwilson Server:" "$MTWILSON_SERVER"
update_property_in_file "mtwilson.server" "$DIRECTOR_PROPERTIES_FILE" "$MTWILSON_SERVER"
prompt_with_default MTWILSON_SERVER_PORT "Mtwilson Server Port:" "$MTWILSON_SERVER_PORT"
update_property_in_file "mtwilson.server.port" "$DIRECTOR_PROPERTIES_FILE" "$MTWILSON_SERVER_PORT"
prompt_with_default MTWILSON_USERNAME "Mtwilson Username:" "$MTWILSON_USERNAME"
update_property_in_file "mtwilson.username" "$DIRECTOR_PROPERTIES_FILE" "$MTWILSON_USERNAME"
prompt_with_default_password MTWILSON_PASSWORD "Mtwilson Password:" "$MTWILSON_PASSWORD"
update_property_in_file "mtwilson.password" "$DIRECTOR_PROPERTIES_FILE" "$MTWILSON_PASSWORD"



prompt_with_default MTWILSON_API_URL "Mtwilson API Url:" "$MTWILSON_API_URL"
update_property_in_file "mtwilson.api.url" "$MTWILSON_PROPERTIES_FILE" "$MTWILSON_API_URL"
update_property_in_file "mtwilson.api.username" "$MTWILSON_PROPERTIES_FILE" "$MTWILSON_USERNAME"
update_property_in_file "mtwilson.api.password" "$MTWILSON_PROPERTIES_FILE" "$MTWILSON_PASSWORD"
prompt_with_default MTWILSON_TLS "Mtwilson TLS:" "$MTWILSON_TLS"
update_property_in_file "mtwilson.api.tls.policy.certificate.sha1" "$MTWILSON_PROPERTIES_FILE" "$MTWILSON_TLS"


#KMS
update_property_in_file "kms.endpoint.url" "$KMS_PROPERTIES_FILE" "$KMS_ENDPOINT_URL"
update_property_in_file "kms.tls.policy.certificate.sha1" "$KMS_PROPERTIES_FILE" "$KMS_TLS_POLICY_CERTIFICATE_SHA1"
update_property_in_file "kms.login.basic.username" "$KMS_PROPERTIES_FILE" "$KMS_LOGIN_BASIC_USERNAME"


#############################################
#update for TDAAS: Write 2 different files for KMS andd MtWilson settings
#############################################
#MtWilson
update_property_in_file "mtwilson.server" "$MTWILSON_PROPERTIES_FILE" "$MTWILSON_SERVER"
update_property_in_file "mtwilson.server.port" "$MTWILSON_PROPERTIES_FILE" "$MTWILSON_SERVER_PORT"
update_property_in_file "mtwilson.username" "$MTWILSON_PROPERTIES_FILE" "$MTWILSON_USERNAME"
update_property_in_file "mtwilson.password" "$MTWILSON_PROPERTIES_FILE" "$MTWILSON_PASSWORD"

#KMS
update_property_in_file "kms.endpoint.url" "$KMS_PROPERTIES_FILE" "$KMS_ENDPOINT_URL"
update_property_in_file "kms.tls.policy.certificate.sha1" "$KMS_PROPERTIES_FILE" "$KMS_TLS_POLICY_CERTIFICATE_SHA1"
update_property_in_file "kms.login.basic.username" "$KMS_PROPERTIES_FILE" "$KMS_LOGIN_BASIC_USERNAME"


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
DIRECTOR_YUM_PACKAGES="zip unzip authbind qemu-utils expect openssl sshfs kpartx libguestfs-tools lvm2"
DIRECTOR_APT_PACKAGES="zip  unzip authbind qemu-utils expect openssl sshfs kpartx libguestfs-tools lvm2" #vdfuse"
DIRECTOR_YAST_PACKAGES="zip unzip authbind qemu-utils expect openssl sshfs kpartx libguestfs-tools lvm2"
DIRECTOR_ZYPPER_PACKAGES="zip  unzip authbind qemu-utils expect openssl sshfs kpartx libguestfs-tools lvm2"
auto_install "Installer requirements" "DIRECTOR"
if [ $? -ne 0 ]; then echo_failure "Failed to install prerequisites through package installer"; exit -1; fi


export postgres_required_version=${POSTGRES_REQUIRED_VERSION:-9.3}

 postgres_write_connection_properties "$DIRECTOR_CONFIGURATION/director.properties" director.db
 

 
  postgres_installed=1
  touch ${DIRECTOR_HOME}/.pgpass
  chmod 0600 ${DIRECTOR_HOME}/.pgpass
  chown ${DIRECTOR_USERNAME}:${DIRECTOR_USERNAME} ${DIRECTOR_HOME}/.pgpass
  export POSTGRES_HOSTNAME POSTGRES_PORTNUM POSTGRES_DATABASE POSTGRES_USERNAME POSTGRES_PASSWORD
  if [ "$POSTGRES_HOSTNAME" == "127.0.0.1" ] || [ "$POSTGRES_HOSTNAME" == "localhost" ]; then
    PGPASS_HOSTNAME=localhost
  else
    PGPASS_HOSTNAME="$POSTGRES_HOSTNAME"
  fi
  echo "$POSTGRES_HOSTNAME:$POSTGRES_PORTNUM:$POSTGRES_DATABASE:$POSTGRES_USERNAME:$POSTGRES_PASSWORD" > ${DIRECTOR_HOME}/.pgpass
  echo "$PGPASS_HOSTNAME:$POSTGRES_PORTNUM:$POSTGRES_DATABASE:$POSTGRES_USERNAME:$POSTGRES_PASSWORD" >> ${DIRECTOR_HOME}/.pgpass
  if [ $(whoami) == "root" ]; then cp ${DIRECTOR_HOME}/.pgpass ~/.pgpass; fi
 

 if [ "$(whoami)" == "root" ]; then

    # Copy the www.postgresql.org PGP public key so add_postgresql_install_packages can add it later if needed
    if [ -d "/etc/apt" ]; then
      mkdir -p /etc/apt/trusted.gpg.d
      chmod 755 /etc/apt/trusted.gpg.d
      cp ACCC4CF8.asc "/etc/apt/trusted.gpg.d"
      POSTGRES_SERVER_APT_PACKAGES="postgresql-9.3"
      add_postgresql_install_packages "POSTGRES_SERVER"
    fi
	echo "opt postgres value is:: $opt_postgres"
         if [[ "$POSTGRES_HOSTNAME" == "127.0.0.1" || "$POSTGRES_HOSTNAME" == "localhost" || -n `echo "$(hostaddress_list)" | grep "$POSTGRES_HOSTNAME"` ]]; then
        echo "Installing postgres server..."
        # when we install postgres server on ubuntu it prompts us for root pw
        # we preset it so we can send all output to the log
        aptget_detect; dpkg_detect; yum_detect;
        if [[ -n "$aptget" ]]; then
          echo "postgresql app-pass password $POSTGRES_PASSWORD" | debconf-set-selections 
        fi
        postgres_installed=0 #postgres is being installed
        # don't need to restart postgres server unless the install script says we need to (by returning zero)
        if postgres_server_install; then
          postgres_restart >> $INSTALL_LOG_FILE
          sleep 10
        fi
        # postgres server end
      fi 
      # postgres client install here
      echo "Installing postgres client..."
      postgres_install
      # do not need to restart postgres server after installing the client.
      #postgres_restart >> $INSTALL_LOG_FILE
      #sleep 10
      echo "Installation of postgres client complete" 
      # postgres client install end
    
  
  fi
  
if [ -z "$SKIP_DATABASE_INIT" ]; then
    # postgres db init here
	echo "Creating logs for mtwilson"
	mkdir -p /opt/mtwilson/logs
	chmod 777 /opt/mtwilson/*
	
    postgres_create_database
    if [ $? -ne 0 ]; then
      echo_failure "Cannot create database"
      exit 1
    fi
    #postgres_restart >> $INSTALL_LOG_FILE
    #sleep 10
    #export is_postgres_available postgres_connection_error
    if [ -z "$is_postgres_available" ]; then
      echo_warning "Run 'director setup' after a database is available"; 
    fi
    # postgress db init end
  else
    echo_warning "Skipping init of database"
  fi
  if [ $postgres_installed -eq 0 ]; then
    postgres_server_detect
    has_local_peer=`grep "^local.*all.*all.*peer" $postgres_pghb_conf`
    if [ -n "$has_local_peer" ]; then
      echo "Replacing PostgreSQL local 'peer' authentication with 'password' authentication..."
      sed -i 's/^local.*all.*all.*peer/local all all password/' $postgres_pghb_conf
    fi
    #if [ "$POSTGRESQL_KEEP_PGPASS" != "true" ]; then   # Use this line after 2.0 GA, and verify compatibility with other commands
    if [ "${POSTGRESQL_KEEP_PGPASS:-true}" == "false" ]; then
      if [ -f ${DIRECTOR_HOME}/.pgpass ]; then
        echo "Removing .pgpass file to prevent insecure database password storage in plaintext..."
        rm -f ${DIRECTOR_HOME}/.pgpass
        if [ $(whoami) == "root" ]; then rm -f ~/.pgpass; fi
      fi
    fi
  fi
 
 
 
 
 



 
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



 
postgres_test_dbtables(){
is_postgres_tables_available=""
echo "inside db table created  check method"
res_tables=`sudo -u postgres  psql postgres  -d ${POSTGRES_DATABASE} -c "SELECT table_name FROM information_schema.tables ORDER BY table_name;"|grep -x "\s*mw_image\s*"`
echo "result of db table exist ${res_tables}"
if [ -n "$res_tables" ]; then
   is_postgres_tables_available="yes"
   return 0
	
fi	

return 1

}


postgres_test_dbtables
if [ -n "$is_postgres_tables_available" ]; then
    echo_success "Database tables in [${POSTGRES_DATABASE}] already exists"
    return 0
  else
   echo "before running scripts"
        #cp psql.sql /tmp
        #chmod 777 /tmp/psql.sql
        psql -h ${POSTGRES_HOSTNAME:-$DEFAULT_POSTGRES_HOSTNAME} -p ${POSTGRES_PORTNUM:-$DEFAULT_POSTGRES_PORTNUM} -d ${POSTGRES_DATABASE:-$DEFAULT_POSTGRES_DATABASE} -U ${POSTGRES_USERNAME:-$DEFAULT_POSTGRES_USERNAME} -w -a -f psql.sql 2>/tmp/intel.postgres.err >> $INSTALL_LOG_FILE
        #sudo -u postgres  psql postgres  -d ${POSTGRES_DATABASE} -a -f  "/tmp/psql.sql">> $INSTALL_LOG_FILE
	echo "after running scripts"
fi


# register linux startup script
register_startup_script $DIRECTOR_HOME/bin/director.sh director





# setup the director, unless the NOSETUP variable is defined
if [ -z "$DIRECTOR_NOSETUP" ]; then
  # the master password is required
  if [ -z "$DIRECTOR_PASSWORD" ] && [ ! -f $DIRECTOR_CONFIGURATION/.director_password ]; then
    director generate-password > $DIRECTOR_CONFIGURATION/.director_password
  fi

  director config mtwilson.extensions.fileIncludeFilter.contains "${MTWILSON_EXTENSIONS_FILEINCLUDEFILTER_CONTAINS:-mtwilson,director}" >/dev/null
  director config mtwilson.extensions.packageIncludeFilter.startsWith "${MTWILSON_EXTENSIONS_PACKAGEINCLUDEFILTER_STARTSWITH:-com.intel,org.glassfish.jersey.media.multipart}" >/dev/null

  #TODO:  customize for director tabs (using feature-id)  and default/home tab (use element id of the target tab)
 director config mtwilson.navbar.buttons director-html5,mtwilson-core-html5 >/dev/null
 director config mtwilson.navbar.hometab director-trust-policy >/dev/null

  director config jetty.port ${JETTY_PORT:-80} >/dev/null
  director config jetty.secure.port ${JETTY_SECURE_PORT:-443} >/dev/null

  director setup
fi

# delete the temporary setup environment variables file
rm -f $DIRECTOR_ENV/director-setup

# ensure the director owns all the content created during setup
for directory in $DIRECTOR_HOME $DIRECTOR_CONFIGURATION $DIRECTOR_JAVA $DIRECTOR_BIN $DIRECTOR_ENV $DIRECTOR_REPOSITORY $DIRECTOR_LOGS; do
  chown -R $DIRECTOR_USERNAME:$DIRECTOR_USERNAME $directory
done

# start the server, unless the NOSETUP variable is defined
if [ -z "$DIRECTOR_NOSETUP" ]; then director start; fi
echo_success "Installation complete"
