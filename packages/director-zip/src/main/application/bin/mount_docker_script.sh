#!/bin/sh
#
#docker Storage drive related info can be found with "docker info" command
#metadata of image can be find with "cat /va/lib/docker/devicemapper/metadata/full-imageid"

# Function of script
# Mount the docker image at specified location provided with table info of snapshot of instance 
# which contains "0 size_of_snapshot thin pool_path snapshot_id"
# this table can be used to create a volume from snapshot and later this volume can be mounted at specified location
# Required arguments are: image_id, snapshot_table_info, mount_path
set -x

IMAGE_ID=""
MOUNT_PATH=""
FUNCTION=""
SNAPSHOT_TABLE_INFO=""

get_snapshot_table_info() {
	DOCKER_POOL=$(docker info | grep "Pool Name" | awk '{print $3}')
	METADATA_DIR=$(docker info | grep "Metadata loop" | awk '{print $4}')
	METADATA_FILE="/var/lib/docker/devicemapper/metadata/$IMAGE_ID"
	DEVICE_ID=$(cat $METADATA_FILE | awk -F'\"device_id\":|,' '{ print $2 }')
	SIZE=$(cat $METADATA_FILE | awk -F'\"size\":|,' '{ print $3 }')
        SNAPSHOT_TABLE_INFO="0 $SIZE thin /dev/mapper/$DOCKER_POOL $DEVICE_ID"
}

mount_device_mapper() {
	IMAGE_ID=$(docker images --no-trunc | awk -v rep="$REPOSITORY" '$1 == rep' | awk -v tag="$TAG" '$2 == tag {print $3}')
	get_snapshot_table_info
	# create volume of snapshot
	# if successful volume will be created under /dev/mapper/
	dmsetup create $IMAGE_ID --table "${SNAPSHOT_TABLE_INFO}"
	#redirect the output to log file
	if [ `echo $?` -ne 0 ]
	then
		echo "error in creating the volume from given snapshot table info"
		exit 1
	else
		echo "Successfully created the volume from given snapshot table info"
	fi
	#mount the volume to specified location
	mount "/dev/mapper/"${IMAGE_ID} $MOUNT_PATH
	if [ `echo $?` -ne 0 ]
	then
		echo "can't mount the volume at specified location"
		exit 2
	else
		echo "Volume successfully mounted at specified location"
	fi
}

unmount_device_mapper() {
	IMAGE_ID=$(docker images --no-trunc | awk -v rep="$REPOSITORY" '$1 == rep' | awk -v tag="$TAG" '$2 == tag {print $3}')
	# unmount the volume
	umount $MOUNT_PATH
	# remove the volume
	dmsetup remove "/dev/mapper/${IMAGE_ID}"
}

mount_aufs() {
	IMAGE_ID=$(docker images --no-trunc | awk -v rep="$REPOSITORY" '$1 == rep' | awk -v tag="$TAG" '$2 == tag {print $3}')
	DOCKER_AUFS_PATH="/var/lib/docker/aufs"
	DOCKER_AUFS_LAYERS="${DOCKER_AUFS_PATH}/layers"
	DOCKER_AUFS_DIFF="${DOCKER_AUFS_PATH}/diff"
	BRANCH="br:${DOCKER_AUFS_DIFF}/${IMAGE_ID}=rw+wh"
	while read LAYER; do
  		BRANCH="${BRANCH}:${DOCKER_AUFS_DIFF}/${LAYER}=rw+wh"
	done < "${DOCKER_AUFS_LAYERS}/${IMAGE_ID}"

	mount -t aufs -o "${BRANCH}" "${IMAGE_ID}" "${MOUNT_PATH}"
}

unmount_aufs() {
	umount $MOUNT_PATH
}

usage()
{
	echo "Usage: \n"
	echo "To mount the image: $0 mount MOUNT_POINT REPOSITORY TAG\n"
	echo "To unmount the image: $0 unmount MOUNT_PATH REPOSITORY TAG\n"
	echo "$0 --help print this message"
}


if [ $# -eq 4 ]
then
	FUNCTION="$1"
	MOUNT_PATH="$2"
	REPOSITORY="$3"
	TAG="$4"
	if [ -z "$FUNCTION" ] || [ -z "$REPOSITORY" ] || [ -z "$TAG" ] || [ -z "$MOUNT_PATH" ] 
	then
		echo "Please provide valid arguments, one of the arguments is empty"
		usage
		exit -1
	fi
	STORAGE_DRIVER=$(docker info | grep "Storage" | awk '{print $3}')
	if [ "$FUNCTION" = "mount" ]
	then
		mkdir -p $MOUNT_PATH
		if [ "$STORAGE_DRIVER" = "devicemapper" ]
		then
			mount_device_mapper
		elif [ "$STORAGE_DRIVER" = "aufs" ]
		then
			mount_aufs
		else
			echo "Unsupported storage driver : $STORAGE_DRIVER"
			exit -1
		fi
	elif [ "$FUNCTION" = "unmount" ]
	then
		if [ "$STORAGE_DRIVER" = "devicemapper" ]
                then
                        unmount_device_mapper
                elif [ "$STORAGE_DRIVER" = "aufs" ]
                then
                        unmount_aufs
                else
                        echo "Unsupported storage driver : $STORAGE_DRIVER"
                        exit -1
                fi
	else
		echo "Invalid fucntion : $FUNCTION"
		usage
	fi 
elif [ $# -eq 1 ] && [ "$1" == "--help" ]
then
	usage
else
	echo "Invalid arguments..."
	usage
fi
