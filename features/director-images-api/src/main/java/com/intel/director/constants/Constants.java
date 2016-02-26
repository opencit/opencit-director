/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.intel.director.constants;


/**
 *
 * @author root
 */
public class Constants {
	


	public static final String mountScript = "/opt/director/bin/mount_vm_image.sh";
	public static final String mountRemoteFileSystemScript = "/opt/director/bin/mount_remote_system.sh";
	public static final String encryptImageScript = "/opt/director/bin/encrypt_image.sh";
	public static final String mountPath = "/mnt/director/";
	public static final String uploadPath = "director.upload.path";
	public static final String defaultUploadPath = "/mnt/images/";
	public static final String vmImagesPath = "/opt/kms/images/vm/";
	public static final String configurationPath = "/opt/director/configuration/";
	public static final String DOCKER_EXECUTABLES = "docker";
	public static final String mountDockerScript = "/opt/director/bin/mount_docker_script.sh";
	public static final String rmiDockerImage = "/opt/director/bin/rmi_docker_image.sh";
	public static final String dockerPolicyInject = "/opt/director/bin/docker_policy_inject.sh";
	public static final String VISIBILITY="visibility";

	public static final String LAUNCH_CONTROL_POLICY_HASH_ONLY = "MeasureOnly";
	public static final String LAUNCH_CONTROL_POLICY_HASH_AND_ENFORCE = "MeasureAndEnforce";
	public static final String SUCCESS="success";
	public static final String CUSTOMER_ID = "Customer ID";
	public static final String IMAGE_NAME = "Image Name";
	public static final String IMAGE_ID = "Image ID";
	public static final String TRUST_POLICY_PATH = "Trust Policy Path";
	public static final String HASH_TYPE = "Hash_Type";
	public static final String Mt_WILSON_IP = "Mt_Wilson_IP";
	public static final String Mt_WILSON_PORT = "Mt_Wilson_Port";
	public static final String IMAGE_LOCATION = "Image Location";
	public static final String TRUST_POLICY_LOCATION = "Trust Policy Location";
	public static final String POLICY_TYPE = "Policy Type";
	public static final String IMAGE_TYPE = "Image Type";
	public static final String KERNEL_PATH = "Kernel Path";
	public static final String INITRD_PATH = "Initrd Path";
	public static final String HIDDEN_FILES = "Hidden Files";
	public static final String Enc_IMAGE_LOCATION = "EncImage Location";
	public static final String Enc_KERNEL_PATH = "EncKernel Path";
	public static final String Enc_INITRD_PATH = "EncInitrd Path";
	
	public static final String remoteSystemIpAddress = "IP Address";
	public static final String remoteSystemuserName = "User Name";
	public static final String remoteSystemPassword = "Password";

	public static final String IS_ENCRYPTED = "Is_Encrypted";
	public static final String BARE_METAL = "Bare_Metal";
	public static final String BARE_METAL_REMOTE = "Bare_Metal_Remote";

	public static final String CONTAINER_FORMAT = "Container Format";
	public static final String DISK_FORMAT = "Disk Format";
	public static final String IS_PUBLIC = "Is Public";
	public static final String NAME = "Name";
	// public static final String IMAGE_ID = "Image ID";
	public static final String KERNEL_ID = "Kernel ID";
	public static final String INITRD_ID = "Initrd ID";

	public static final String USER_NAME = "User_Name";
	public static final String PASSWORD = "Password";
	public static final String TENANT_NAME = "Tenant_Name";

	public static final String MOUNT_PATH = "/tmp/mount";

	public static final String EncPASSWORD = "intelrp";

	public static final String IS_WINDOWS = "Is Windows";

	public static final String MH_KEY_NAME = "MH_Key_Name";

	public static final String MH_JAR_LOCATION = "MH_Jar_Location";

	public static final String EXEC_OUTPUT_FILE = "./log/exec_output";

	public static final String KMS_SERVER_IP = "KMS_Server_IP";

	public static final String MH_DEK_URL_IMG = "MH Dek Url Image";

	public static final String MH_DEK_URL_KERNEL = "MH Dek Url Kernel";

	public static final String MH_DEK_URL_INITRD = "MH Dek Url Initrd";

	public static final String Mt_WILSON_USER_NAME = "Mt_Wilson_User_Name";
	public static final String Mt_WILSON_PASSWORD = "Mt_Wilson_Password";
	public static final String EXCLUDE_FILE_NAME = "./resources/exclude-file-list";
	public static final String HOST_MANIFEST = "Host_Manifest";
	public static final String MH_KEYSTORE_PASSWD = "MH_Keystore_Password";
	public static final String MH_KEYSTORE_LOCATION = "MH_Keystore_Location";
	public static final String MH_TLS_SSL_PASSWD = "MH_TLS_SSL_Passwd";

	public static final String TARBALL_PATH = "/tmp/";
	public static final String IMAGE_STORE_TYPE = "image.store.type";
	
	
	public static final String GLANCE_IMAGE_STORE = "Openstack_Glance";
	public static final String GLANCE_IMAGE_STORE_SERVER = "glance.image.store.server";
	public static final String GLANCE_IMAGE_STORE_USERNAME = "glance.image.store.username";
	public static final String GLANCE_IMAGE_STORE_PASSWORD = "glance.image.store.password";
	public static final String GLANCE_TENANT_NAME = "glance.tenant.name";
	public static final String GLANCE_API_ENDPOINT = "glance.api.endpoint";
	public static final String GLANCE_KEYSTONE_PUBLIC_ENDPOINT = "glance.keystone.public.endpoint";
	public static final String DIRECTOR_ID = "director.id";
	public static final String CONTENT_LENGTH = "Content-Length";
	public static final String GLANCE_HEADER_CREATED_AT = "X-Image-Meta-Created_at";
	public static final String GLANCE_HEADER_DISK_FORMAT = "X-Image-Meta-Disk_format";
	public static final String GLANCE_HEADER_STATUS = "X-Image-Meta-Status";
	public static final String GLANCE_HEADER_LOCATION = "Location";
	public static final String GLANCE_HEADER_CHECKSUM = "Image-Meta-Checksum";
	public static final String GLANCE_HEADER_CONTAINER_FORMAT = "X-Image-Meta-Container_format";
	public static final String GLANCE = "Glance";
	public static final String GLANCE_ID="glanceid";
	public static final String CHECKSUM="checksum";
	public static final String SELF="self";
	public static final String SIZE="size";
	
	public static final String SWIFT = "Swift";
	public static final String SWIFT_API_ENDPOINT = "swift.api.endpoint";
	public static final String SWIFT_IP = "swift.ip";
	public static final String SWIFT_USERNAME = "swift.usename";
	public static final String SWIFT_PASSWORD = "swift.password";
	public static final String SWIFT_TENANT_NAME = "swift.tenant.name";
	public static final String SWIFT_API_VERSION = "v1.0";
	public static final String SWIFT_CONTAINER_NAME = "swift.container.name";
	public static final String SWIFT_OBJECT_NAME = "swift.object.name";
	public static final String SWIFT_ACCOUNT_NAME = "swift.account.name";
	public static final String SWIFT_ACCOUNT_USERNAME = "swift.account.usename";
	public static final String SWIFT_ACCOUNT_USER_PASSWORD = "swift.account.userpassword";
	public static final String SWIFT_STORAGE_USER = "X-Storage-User";
	public static final String SWIFT_STORAGE_PASSWORD = "X-Storage-Pass";
	public static final String SWIFT_PORT = "swift.port";
	public static final String AUTH_TOKEN = "X-AUTH-TOKEN";
	public static final String SWIFT_STORAGE_URL ="X-Storage-Url";
	public static final String SWIFT_WRITE_TO_FILE_PATH ="swift.write.to.file.path";
	public static final String SWIFT_POLICY_PATH = "/policy";
	public static final String SWIFT_PATH = "swift.path";
	public static final String SWIFT_POLICY_CONTAINER_NAME = "trust_policies";
	public static final String SWIFT_DOWNLOAD_FILE_NAME = "swift.download.file.name";
	public static final String SWIFT_DOWNLOAD_FILE_PATH = "swift.download.file.path";

	public static final String TASK_NAME_CREATE_TAR = "Create Tar";
	public static final String TASK_NAME_PUSH_POLICY = "Push Policy";
	public static final String TASK_NAME_UPLOAD_POLICY = "Upload Policy";
	public static final String TASK_NAME_UPLOAD_IMAGE = "Upload Image";
	public static final String TASK_NAME_ENCRYPT_IMAGE = "Encrypt Image";
	public static final String TASK_NAME_UPLOAD_TAR = "Upload Tar";
	public static final String TASK_NAME_INJECT_POLICY = "Inject Policy";
	public static final String COMPLETE = "Complete";
	public static final String INCOMPLETE = "Incomplete";
	public static final String IN_PROGRESS = "In Progress";
	public static final String ERROR = "Error";


	public static final String DEPLOYMENT_TYPE_VM = "VM";
	public static final String DEPLOYMENT_TYPE_BAREMETAL = "BareMetal";
	public static final String DEPLOYMENT_TYPE_DOCKER = "Docker";
	public static final String INCLUDE_LIST_VM="vm.include";
	public static final String INCLUDE_LIST_BM="bm.include";
	public static final String INCLUDE_LIST_BM_LIVE="bmlive.include";
	public static final String MTWILSON_PROP_FILE = "mtwilson.properties";
	public static final String KMS_PROP_FILE = "kms.properties";
	public static final String MTWILSON_TRUST_POLICY_LOCATION="mtwilson_trustpolicy_location";
	////public static final String MTWILSON_TRUST_POLICY_SWIFT_URI="x-image-meta-property-mtwilson_trustpolicy_swift_uri";

	
	public static final String DIRECTOR_DB_URL="director.db.url";
	public static final String DIRECTOR_DB_USERNAME="director.db.username";
	public static final String DIRECTOR_DB_PASSWORD="director.db.password";
	public static final String DIRECTOR_DB_DRIVER="director.db.driver";
	public static final String ARTIFACT_IMAGE="Image";
	public static final String ARTIFACT_TAR="Tarball";
	public static final String ARTIFACT_POLICY="Policy";
	public static final String ARTIFACT_IMAGE_WITH_POLICY="ImageWithPolicy";
	public static final String ARTIFACT_DOCKER = "Docker";
	public static final String ARTIFACT_DOCKER_IMAGE="DockerImage";
	public static final String ARTIFACT_DOCKER_WITH_POLICY="DockerTarball";
	
	
	public static final String CONNECTOR_GLANCE = "Glance";
	public static final String CONNECTOR_SWIFT = "Swift";
	public static final String CONNECTOR_DOCKERHUB = "Docker";
	
	public static final String UPLOAD_TO_IMAGE_STORE_FILE = "UPLOAD_TO_IMAGE_STORE_FILE";
	
	public static final String DOCKER_HUB_USERNAME = "Username";
	public static final String DOCKER_HUB_PASSWORD = "Password";
	public static final String DOCKER_HUB_EMAIL = "Email";

}