

########## FUNCTIONS LIBRARY ##########
load_director_conf() {
  DIRECTOR_PROPERTIES_FILE=${DIRECTOR_PROPERTIES_FILE:-"/opt/director/configuration/director.properties"}
  if [ -n "$DEFAULT_ENV_LOADED" ]; then return; fi

  # director.properties file
  if [ -f "$DIRECTOR_PROPERTIES_FILE" ]; then
    echo -n "Reading properties from file [$DIRECTOR_PROPERTIES_FILE]....."
    export CONF_DIRECTOR_ID=$(read_property_from_file "director.id" "$DIRECTOR_PROPERTIES_FILE")
    export CONF_VM_WHITELIST_HASH_TYPE=$(read_property_from_file "vm.whitelist.hash.type" "$DIRECTOR_PROPERTIES_FILE")
    export CONF_IMAGE_STORE_TYPE=$(read_property_from_file "image.store.type" "$DIRECTOR_PROPERTIES_FILE")
    export CONF_IMAGE_STORE_SERVER=$(read_property_from_file "image.store.server" "$DIRECTOR_PROPERTIES_FILE")
    export CONF_IMAGE_STORE_USERNAME=$(read_property_from_file "image.store.username" "$DIRECTOR_PROPERTIES_FILE")
    export CONF_IMAGE_STORE_PASSWORD=$(read_property_from_file "image.store.password" "$DIRECTOR_PROPERTIES_FILE")
    export CONF_TENANT_NAME=$(read_property_from_file "tenant.name" "$DIRECTOR_PROPERTIES_FILE")
    export CONF_MTWILSON_SERVER=$(read_property_from_file "mtwilson.server" "$DIRECTOR_PROPERTIES_FILE")
    export CONF_MTWILSON_SERVER_PORT=$(read_property_from_file "mtwilson.server.port" "$DIRECTOR_PROPERTIES_FILE")
    export CONF_MTWILSON_USERNAME=$(read_property_from_file "mtwilson.username" "$DIRECTOR_PROPERTIES_FILE")
    export CONF_MTWILSON_PASSWORD=$(read_property_from_file "mtwilson.password" "$DIRECTOR_PROPERTIES_FILE")
    echo_success "Done"
  fi

  export DEFAULT_ENV_LOADED=true
  return 0
}

load_director_defaults() {
  export DEFAULT_DIRECTOR_ID=""
  export DEFAULT_VM_WHITELIST_HASH_TYPE="SHA-1"
  export DEFAULT_IMAGE_STORE_TYPE=""
  export DEFAULT_IMAGE_STORE_SERVER=""
  export DEFAULT_IMAGE_STORE_USERNAME=""
  export DEFAULT_IMAGE_STORE_PASSWORD=""
  export DEFAULT_TENANT_NAME=""
  export DEFAULT_MTWILSON_SERVER=""
  export DEFAULT_MTWILSON_SERVER_PORT=""
  export DEFAULT_MTWILSON_USERNAME=""
  export DEFAULT_MTWILSON_PASSWORD=""

  export DIRECTOR_ID=${DIRECTOR_ID:-${CONF_DIRECTOR_ID:-$DEFAULT_DIRECTOR_ID}}
  export VM_WHITELIST_HASH_TYPE=${VM_WHITELIST_HASH_TYPE:-${CONF_VM_WHITELIST_HASH_TYPE:-$DEFAULT_VM_WHITELIST_HASH_TYPE}}
  export IMAGE_STORE_TYPE=${IMAGE_STORE_TYPE:-${CONF_IMAGE_STORE_TYPE:-$DEFAULT_IMAGE_STORE_TYPE}}
  export IMAGE_STORE_SERVER=${IMAGE_STORE_SERVER:-${CONF_IMAGE_STORE_SERVER:-$DEFAULT_IMAGE_STORE_SERVER}}
  export IMAGE_STORE_USERNAME=${IMAGE_STORE_USERNAME:-${CONF_IMAGE_STORE_USERNAME:-$DEFAULT_IMAGE_STORE_USERNAME}}
  export IMAGE_STORE_PASSWORD=${IMAGE_STORE_PASSWORD:-${CONF_IMAGE_STORE_PASSWORD:-$DEFAULT_IMAGE_STORE_PASSWORD}}
  export TENANT_NAME=${TENANT_NAME:-${CONF_TENANT_NAME:-$DEFAULT_TENANT_NAME}}
  export MTWILSON_SERVER=${MTWILSON_SERVER:-${CONF_MTWILSON_SERVER:-$DEFAULT_MTWILSON_SERVER}}
  export MTWILSON_SERVER_PORT=${MTWILSON_SERVER_PORT:-${CONF_MTWILSON_SERVER_PORT:-$DEFAULT_MTWILSON_SERVER_PORT}}
  export MTWILSON_USERNAME=${MTWILSON_USERNAME:-${CONF_MTWILSON_USERNAME:-$DEFAULT_MTWILSON_USERNAME}}
  export MTWILSON_PASSWORD=${MTWILSON_PASSWORD:-${CONF_MTWILSON_PASSWORD:-$DEFAULT_MTWILSON_PASSWORD}}
}

director_java_install() {
  DIRECTOR_INSTALL_LOG_FILE=${DIRECTOR_INSTALL_LOG_FILE:-"/var/log/director/director_install.log"}
  java_clear; java_detect 2>&1 >> $DIRECTOR_INSTALL_LOG_FILE
  JAVA_PACKAGE=$(ls -1d jdk*)
  if [[ -z "$JAVA_PACKAGE" || ! -f "$JAVA_PACKAGE" ]]; then
    echo_failure "Missing Java installer: $JAVA_PACKAGE" | tee -a 
    return 1
  fi
  javafile=$JAVA_PACKAGE
  echo "Installing $javafile" >> $DIRECTOR_INSTALL_LOG_FILE
  is_targz=$(echo $javafile | grep -E ".tar.gz$|.tgz$")
  is_gzip=$(echo $javafile | grep ".gz$")
  is_bin=$(echo $javafile | grep ".bin$")
  javaname=$(echo $javafile | awk -F . '{ print $1 }')
  if [ -n "$is_targz" ]; then
    tar xzvf $javafile 2>&1 >> $DIRECTOR_INSTALL_LOG_FILE
  elif [ -n "$is_gzip" ]; then
    gunzip $javafile 2>&1 >/dev/null >> $DIRECTOR_INSTALL_LOG_FILE
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

  java_detect 2>&1 >> $DIRECTOR_INSTALL_LOG_FILE
  if [[ -z "$JAVA_HOME" || -z "$java" ]]; then
    echo_failure "Unable to auto-install Java" | tee -a $DIRECTOR_INSTALL_LOG_FILE
    echo "  Java download URL:"                >> $DIRECTOR_INSTALL_LOG_FILE
    echo "  http://www.java.com/en/download/"  >> $DIRECTOR_INSTALL_LOG_FILE
  fi
}