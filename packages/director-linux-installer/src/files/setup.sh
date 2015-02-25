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

DIRECTOR_PROPERTIES_FILE=${DIRECTOR_PROPERTIES_FILE:-"/opt/trustdirector/configuration/director.properties"}
INSTALL_LOG_DIRECTORY="/var/log/trustdirector"
INSTALL_LOG_FILE="$INSTALL_LOG_DIRECTORY/trustdirector_install.log"
mkdir -p "$INSTALL_LOG_DIRECTORY"

load_director_conf() {
  DIRECTOR_PROPERTIES_FILE=${DIRECTOR_PROPERTIES_FILE:-"/opt/trustdirector/configuration/director.properties"}
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
}

#handle encryption
load_director_conf
load_director_defaults
if [ -f /root/director.env ]; then  . /root/director.env; fi
if [ -f director.env ]; then . director.env; fi

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

export DIRECTOR_OWNER=${DIRECTOR_OWNER:-director}
getent passwd $MTWILSON_OWNER >/dev/null
if [ $? != 0 ]; then
  echo "Creating Director owner account [$DIRECTOR_OWNER]"
  useradd -s /bin/false -d /opt/trustdirector $DIRECTOR_OWNER
  if [ $? != 0 ]; then
    echo_warning "Failed to create user '$DIRECTOR_OWNER'"
  fi
fi

DIRECTOR_APT_PACKAGES="qemu-utils expect openssl"
DIRECTOR_YAST_PACKAGES=""
DIRECTOR_YUM_PACKAGES=""
DIRECTOR_ZYPPER_PACKAGES=""
auto_install "Installer requirements" "DIRECTOR"

### INSTALL JAVA
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

mkdir -p /opt/trustdirector/bin
mkdir -p /opt/trustdirector/configuration
mkdir -p /opt/trustdirector/java
cp exclude-file-list /opt/trustdirector/bin/
cp login /opt/trustdirector/bin/
cp mount_remote_system.sh /opt/trustdirector/bin/
cp mount_vm_image.sh /opt/trustdirector/bin/
cp tdirector.sh /opt/trustdirector/bin/tdirector
cp client-0.1-SNAPSHOT-with-dependencies.jar /opt/trustdirector/java/
cp director-javafx-*.jar /opt/trustdirector/java/
chmod 700 /opt/trustdirector/bin/exclude-file-list
chmod 700 /opt/trustdirector/bin/login
chmod 700 /opt/trustdirector/bin/mount_remote_system.sh
chmod 700 /opt/trustdirector/bin/mount_vm_image.sh
chmod 700 /opt/trustdirector/bin/tdirector
chmod 700 retrieve-cert.sh
chmod 700 set-path.sh
#TEMP
mkdir -p /opt/trustdirector/log
touch /opt/trustdirector/log/manifest-tool.log

register_startup_script /opt/trustdirector/bin/tdirector tdirector >> $INSTALL_LOG_FILE

# 1 Configuring KMS TLS certificate and Data encryption key
echo "Importing TLS certficate from KMS server"
mkdir -p /root/.mystery-hill/client
export MHCLIENT_HOME=/root/.mystery-hill/client
# 1.a1 retrieve the certificate
echo " -Retrieving KMS Server certificate"
./retrieve-cert.sh $KMS_SERVER_IP:8443 > /tmp/ssl.crt
# 1.a2 import to keystore
echo " -Importing to Keystore"
java -jar /opt/trustdirector/java/client-0.1-SNAPSHOT-with-dependencies.jar import-tls-certificate --format=PEM /tmp/ssl.crt
rm /tmp/ssl.crt

# 1.b KMS  Data Encryption key certificate
#assuming the KMS data encryption is retrieved and stored at /tmp/kmsDataEncKey
kmsDataEncKey="/tmp/kmsDataEncKey"
echo "Importing Data encryption key from KMS server"
# 1.b1 retrieve
echo " -Retrieving Data encryption key from KMS server"
scp root@$KMS_SERVER_IP:/root/mhserver/private/*.crt $kmsDataEncKey
# 1.b2 import to keystore
echo " -Importing to Keystore"
java -jar /opt/trustdirector/java/client-0.1-SNAPSHOT-with-dependencies.jar import-data-encryption-key-recipient --format=DER $kmsDataEncKey

# 2. Import MTW server configuration
echo "Configuring MtW server certificate"
echo " -Retrieving MTW server certificate"
./retrieve-cert.sh $MTWILSON_SERVER_IP:8443 > /tmp/mtwcert.pem

# 2.a improt to keystore
echo " -Importing the MTW certificate to Keystore"
$JAVA_HOME/jre/bin/keytool -import -noprompt -trustcacerts -alias mtwcert -file /tmp/mtwcert.pem -keystore /usr/lib/jvm/jdk1.7.0_55/jre/lib/security/cacerts
rm /tmp/mtwcert.pem

update_property_in_file "username" "$DIRECTOR_PROPERTIES_FILE" "$USERNAME"
update_property_in_file "password" "$DIRECTOR_PROPERTIES_FILE" "$PASSWORD"
update_property_in_file "tenant.name" "$DIRECTOR_PROPERTIES_FILE" "$TENANT_NAME"
update_property_in_file "mysteryhill.key.name" "$DIRECTOR_PROPERTIES_FILE" "$MYSTERYHILL_KEY_NAME"
update_property_in_file "mysteryhill.keystore" "$DIRECTOR_PROPERTIES_FILE" "$MYSTERYHILL_KEYSTORE"
update_property_in_file "mysteryhill.keystore.password" "$DIRECTOR_PROPERTIES_FILE" "$MYSTERYHILL_KEYSTORE_PASSWORD"
update_property_in_file "mysteryhill.tls.ssl.password" "$DIRECTOR_PROPERTIES_FILE" "$MYSTERYHILL_TLS_SSL_PASSWORD"
update_property_in_file "mtwilson.username" "$DIRECTOR_PROPERTIES_FILE" "$MTWILSON_USERNAME"
update_property_in_file "mtwilson.password" "$DIRECTOR_PROPERTIES_FILE" "$MTWILSON_PASSWORD"
update_property_in_file "mtwilson.server.ip" "$DIRECTOR_PROPERTIES_FILE" "$MTWILSON_SERVER_IP"
update_property_in_file "mtwilson.server.port" "$DIRECTOR_PROPERTIES_FILE" "$MTWILSON_SERVER_PORT"
update_property_in_file "kms.server.ip" "$DIRECTOR_PROPERTIES_FILE" "$KMS_SERVER_IP"
update_property_in_file "glance.ip" "$DIRECTOR_PROPERTIES_FILE" "$GLANCE_IP"
update_property_in_file "hash.type" "$DIRECTOR_PROPERTIES_FILE" "$HASH_TYPE"
update_property_in_file "image.store.type" "$DIRECTOR_PROPERTIES_FILE" "$IMAGE_STORE_TYPE"

#end
echo_success "Installation Complete"
