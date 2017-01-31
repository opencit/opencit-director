#!/bin/bash
#
#docker Storage drive related info can be found with "docker info" command
#metadata of image can be find with "cat /va/lib/docker/devicemapper/metadata/full-imageid"
#location of metadata of image has been changed in docker-1.10 and above

# Function of script
# Mount the docker image at specified location provided with table info of snapshot of instance 
# which contains "0 size_of_snapshot thin pool_path snapshot_id"
# this table can be used to create a volume from snapshot and later this volume can be mounted at specified location
# Required arguments are: image_id, snapshot_table_info, mount_path
#set -x

DOCKER_HOST_ADDR=""
IMAGE_ID=""
MOUNT_PATH=""
FUNCTION=""
SNAPSHOT_TABLE_INFO=""
DOCKER_VERSION=""
DOCKER_ROOT_DIR=""
TAG_REPO=""
STORAGE_DRIVER=""
TEMP_DOCKER_INFO_FILE="/tmp/CIT/docker_info"
TEMP_DOCKER_IMAGES_FILE="/tmp/CIT/docker_images"
DRIVER_DEVICEMAPPER="devicemapper"
DRIVER_AUFS="aufs"
UNMOUNT_FLAG=false
MOUNT_FLAG=false

docker_version() {
	if [ -n "$DOCKER_HOST_ADDR" ]
	then
		DOCKER_VERSION=$(docker --version | awk -F'[ ,]' {' print $3'})
	else
		DOCKER_VERSION=$(docker $DOCKER_HOST_ADDR --version | awk -F'[ ,]' {' print $3'})
	fi
	if [ -n "$DOCKER_VERSION" ]
	then
		return 0
	else
		return 1
	fi
}

#reads various docker configuration related info
populate_docker_info() {
	mkdir -p /tmp/CIT
	if [ "$DOCKER_HOST_ADDR" != "" ]
	then
		if $(docker -H "$DOCKER_HOST_ADDR" info >$TEMP_DOCKER_INFO_FILE 2>/dev/null)
		then
			docker -H "$DOCKER_HOST_ADDR" images --no-trunc >$TEMP_DOCKER_IMAGES_FILE 2>/dev/null
		else
			echo >&2 "Check if docker docker daemon is running"
			return 1
		fi
	else	
		if $(docker info >$TEMP_DOCKER_INFO_FILE 2>/dev/null)
		then
			docker images --no-trunc >$TEMP_DOCKER_IMAGES_FILE 2>/dev/null  
		else
			echo >&2 "Check if docker docker daemon is running"
			return 1
		fi
	fi
	if [ -n "$TAG_REPO" ]; then
		IMAGE_TAG=`echo "$TAG_REPO" | awk -F':' '{ print $2}'`
		IMAGE_REPO=`echo "$TAG_REPO" | awk -F':' '{ print $1}'`
		IMAGE_ID=$(cat $TEMP_DOCKER_IMAGES_FILE | awk -v rep="$IMAGE_REPO" '$1 == rep' | awk -v tag="$IMAGE_TAG" '$2 == tag {print $3}')
		IMAGE_ID=`echo $IMAGE_ID | sed -e "s/[^:]*://g"`
		if [ -z "$IMAGE_ID" ]; then
			return 1
		else
			echo >&2 "IMAGE_ID is : $IMAGE_ID"
		fi
	else
		IMAGE_ID=`cat /tmp/CIT/docker_images | grep "$IMAGE_ID" | awk '{print $3}' | sed -e 's/^[^:]*://'`
		echo >&2 "IMAGE_ID is : $IMAGE_ID"
	fi
	STORAGE_DRIVER=$(cat $TEMP_DOCKER_INFO_FILE | grep "Storage Driver" | awk -F'[ :]' '{ print $4 }')
	if [ "$STORAGE_DRIVER" == "$DRIVER_AUFS" ];then		
		echo >&2 "Storage Driver : $STORAGE_DRIVER" 	
		DOCKER_ROOT_DIR=`cat $TEMP_DOCKER_INFO_FILE | grep -i "^\s*Root Dir" | awk -F'[ :]' '{ print $5}'`
		DOCKER_ROOT_DIR=`dirname $DOCKER_ROOT_DIR`
	elif [ "$STORAGE_DRIVER" == "$DRIVER_DEVICEMAPPER" ]; then
		echo >&2 "Storage Driver : $STORAGE_DRIVER" 	
		DOCKER_ROOT_DIR=`cat $TEMP_DOCKER_INFO_FILE | grep -i "Metadata loop file" | awk -F'[ :]' '{ print $6}' | awk -F'devicemapper' '{ print $1}'`
		#DOCKER_ROOT_DIR=`dirname $DOCKER_ROOT_DIR`
		DOCKER_POOL=$( cat $TEMP_DOCKER_INFO_FILE | grep "Pool Name" | awk '{print $3}')
		#METADATA_DIR=$( cat $TEMP_DOCKER_INFO_FILE | grep "Metadata loop" | awk '{print $4}')
	else
		echo >&2 "Either Storage dirver is not supported by mount script or Storage driver is not specfied in docker info"
		return 2
	fi
}

get_snapshot_table_info() {
	if [ -z "$METADATA_FILE" ]; then
		METADATA_FILE="${DOCKER_DEVICEMAPPER_DIR}/metadata/$IMAGE_ID"
	fi
	#DEVICE_ID=$(cat $METADATA_FILE | awk -F'\"device_id\":|,' '{ print $2 }')
	DEVICE_ID=$(cat $METADATA_FILE | jq .device_id)
	SIZE=$(cat $METADATA_FILE | jq .size)
	#SIZE=$(cat $METADATA_FILE | awk -F'\"size\":|,' '{ print $3 }')
        SNAPSHOT_TABLE_INFO="0 $SIZE thin /dev/mapper/$DOCKER_POOL $DEVICE_ID"
}

#mount docker images if storage driver is devicemapper and docker version is less than 1.10
mount_device_mapper_v1_9() {
	#MAGE_ID=$(docker images --no-trunc | awk -v rep="$REPOSITORY" '$1 == rep' | awk -v tag="$TAG" '$2 == tag {print $3}')
	DOCKER_DEVICEMAPPER_DIR="${DOCKER_ROOT_DIR}/devicemapper/"
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
		return 0
	fi
}

#mount docker images if storage driver is devicemapper and docker version is greater that or equals to 1.10
mount_device_mapper_v1_10() {
	DOCKER_DEVICEMAPPER_DIR="${DOCKER_ROOT_DIR}/devicemapper/"
	NUMBER_OF_LAYERS=`cat $DOCKER_ROOT_DIR/image/devicemapper/imagedb/content/sha256/$IMAGE_ID | jq '.rootfs.diff_ids | length'`
	TOP_LAYER_DIFF_ID=`cat $DOCKER_ROOT_DIR/image/devicemapper/imagedb/content/sha256/$IMAGE_ID | jq .rootfs.diff_ids[$(( $NUMBER_OF_LAYERS - 1 ))]`
	TOP_LAYER_DIFF_ID=`echo $TOP_LAYER_DIFF_ID | tr -d '"'`
	DIFF_INFO_FILE=`grep -rl "$TOP_LAYER_DIFF_ID" $DOCKER_ROOT_DIR/image/devicemapper/layerdb/sha256/`
	CACHE_ID_FILE=`dirname $DIFF_INFO_FILE`
	CACHE_ID_FILE=${CACHE_ID_FILE}/cache-id
	METADATA_FILE=`cat $CACHE_ID_FILE`
	METADATA_FILE="${DOCKER_DEVICEMAPPER_DIR}/metadata/${METADATA_FILE}"
	mount_device_mapper_v1_9
}

#unmount the mounted docker image, if underlying storage driver is devicemapper
unmount_device_mapper() {
	#IMAGE_ID=$(docker images --no-trunc | awk -v rep="$REPOSITORY" '$1 == rep' | awk -v tag="$TAG" '$2 == tag {print $3}')
	if temp_var=$(echo "$MOUNT_PATH" | grep -e "/$")
	then
		#if mount path has / appended to it remove it
		MOUNT_PATH=$(echo "$MOUNT_PATH" | sed -e 's/[\/]*$//')
	fi
	DEVICE=$( mount | grep $MOUNT_PATH | awk '{ print $1}' )
	# unmount the volume
	if umount $MOUNT_PATH; then
		# remove the volume
		if dmsetup remove "$DEVICE"; then
			return 0
		else
			return 1
		fi
	else
		return 1
	fi
}

#mounts the docker image when underlying storage driver is aufs and docker version is less than 1.10
mount_aufs_v1_9() {
	#IMAGE_ID=$(docker images --no-trunc | awk -v rep="$REPOSITORY" '$1 == rep' | awk -v tag="$TAG" '$2 == tag {print $3}')
	DOCKER_AUFS_PATH="$DOCKER_ROOT_DIR/aufs"
	DOCKER_AUFS_LAYERS="${DOCKER_AUFS_PATH}/layers"
	DOCKER_AUFS_DIFF="${DOCKER_AUFS_PATH}/diff"
	BRANCH="br"
	#not using rw+wh, because dont want to modigy the mounted image in any way
	BRANCH="${BRANCH}:${DOCKER_AUFS_DIFF}/${IMAGE_ID}=ro+wh"
	while read LAYER; do
		# br means branch, iw+wh, means branches mouted with rw permission, wh (whiteout) should be used with ro(read only) not with rw
		#can use ro+wh instead of rw+wh
  		BRANCH="${BRANCH}:${DOCKER_AUFS_DIFF}/${LAYER}=ro+wh"
	done < "${DOCKER_AUFS_LAYERS}/${IMAGE_ID}"

	mount -t aufs -o "${BRANCH}" "${IMAGE_ID}" "${MOUNT_PATH}"
}

#mounts the docker image when underlying storage driver is aufs and docker version 1.10 and above
mount_aufs_v1_10(){
	NUMBER_OF_LAYERS=`cat $DOCKER_ROOT_DIR/image/aufs/imagedb/content/sha256/$IMAGE_ID | jq '.rootfs.diff_ids | length'`
	TOP_LAYER_DIFF_ID=`cat $DOCKER_ROOT_DIR/image/aufs/imagedb/content/sha256/$IMAGE_ID | jq .rootfs.diff_ids[$(( $NUMBER_OF_LAYERS - 1 ))]`
	TOP_LAYER_DIFF_ID=`echo $TOP_LAYER_DIFF_ID | tr -d '"'`
	DIFF_INFO_FILE=`grep -rl "$TOP_LAYER_DIFF_ID" $DOCKER_ROOT_DIR/image/aufs/layerdb/sha256/`
	CACHE_ID_FILE=`dirname $DIFF_INFO_FILE`
	CACHE_ID_FILE=${CACHE_ID_FILE}/cache-id
	IMAGE_LAYERS_FILE=`cat $CACHE_ID_FILE`
	DOCKER_AUFS_PATH="$DOCKER_ROOT_DIR/aufs"
	DOCKER_AUFS_LAYERS="${DOCKER_AUFS_PATH}/layers"
	DOCKER_AUFS_DIFF="${DOCKER_AUFS_PATH}/diff"
	BRANCH="br"
	#add path of current layers too to the branch
	BRANCH="${BRANCH}:${DOCKER_AUFS_DIFF}/${IMAGE_LAYERS_FILE}=ro+wh"
	while read LAYER; do
		# br means branch, iw+wh, means branches mouted with rw permission, wh (whiteout) should be used with ro(read only) not with rw
		#can use ro+wh instead of rw+wh
  		BRANCH="${BRANCH}:${DOCKER_AUFS_DIFF}/${LAYER}=ro+wh"
	done < "${DOCKER_AUFS_LAYERS}/${IMAGE_LAYERS_FILE}"

	mount -t aufs -o "${BRANCH}" "${IMAGE_ID}" "${MOUNT_PATH}"
}

#unmounts the mounted docker image, if underlying storage driver is aufs
unmount_aufs() {
	if umount $MOUNT_PATH
	then
		return 0
	else
		return 1
	fi
}

#Identifies the underlying storage driver, docker version, IMAGE-ID from TAG and REPO of image,
#and call specific mount function
start_mount() {
	if ! populate_docker_info
	then
		exit $?
	fi
	mkdir -p $MOUNT_PATH
	if version=$(echo "$DOCKER_VERSION" | grep -e "1.1[0-9]") ;then
		if [ "$STORAGE_DRIVER" == "$DRIVER_AUFS" ] ;then
			mount_aufs_v1_10
			return
		elif [ "$STORAGE_DRIVER" == "$DRIVER_DEVICEMAPPER" ] ;then
			mount_device_mapper_v1_10
			return
		fi 
	elif version=$(echo "$DOCKER_VERSION" |grep -e "1.[0-9]\{1\}\.\?[^0-9a-zA-Z]") ;then
		if [ "$STORAGE_DRIVER" == "$DRIVER_AUFS" ] ;then
			mount_aufs_v1_9
			return
		elif [ "$STORAGE_DRIVER" == "$DRIVER_DEVICEMAPPER" ] ;then
			mount_device_mapper_v1_9
			return
		fi 
	fi
}

#Identifies the underlying storage diriver and call specific unmount function
start_unmount() {
	if ! populate_docker_info
	then
		exit 1
	fi
	if [ "$STORAGE_DRIVER" == "$DRIVER_AUFS" ];then
		unmount_aufs
		return $?
	elif [ "$STORAGE_DRIVER" == "$DRIVER_DEVICEMAPPER" ]; then
		unmount_device_mapper
		return $?
	fi
}

while test $# -gt 0; do
	case "$1" in
		-h|--help)
                        echo "$0 - mount and unmount docker images"
			echo " "
			echo "usage:"
			echo "$0 [options]... MOUNT_PATH [options]..."
			echo " "
			echo "options:"
			echo -e "\t-h, --help                           show brief help"
			echo ""
			echo "unmount:"
			echo -e "\t-u, --unmount-path=UNMOUNT_PATH      unmount the image at UNMOUNT_PATH path"
			echo ""
			echo "mount:"
			echo -e "\t-m, --mount-path=MOUNT_PATH          mount image at given MOUNT_PATH with given image tag:repo or image-id"
			echo -e "\t-r, --repo-tag=repo:tag              repository:tag of image you want to mount"
			echo -e "\t-i, --image-id=image-id              image-id of image you want to mount"
			echo -e "\t-H, --host=tcp://[host]:[port][path]"
			echo -e "\t           or unix://path            Address where docker engine is running"
			exit 0
			;;
		-u)
			shift
			#call unmount function
			MOUNT_PATH=$1
			UNMOUNT_FLAG=true
			shift
			;;
		--unmount-path*)
			MOUNT_PATH=$(echo "$1" | sed -e 's/^[^=]*[=]\?//g')
			UNMOUNT_FLAG=true
                        shift
                        ;;
                -m)
                        shift
                        MOUNT_PATH=$1
			MOUNT_FLAG=true
                        shift
                        ;;
                --mount-path*)
                        MOUNT_PATH=`echo $1 | sed -e 's/^[^=]*[=]\?//g'`
			MOUNT_FLAG=true
                        shift
                        ;;
		-r)
			shift
			TAG_REPO=$1
			shift
			;;
		--repo-tag*)
			TAG_REPO=`echo $1 | sed -e 's/^[^=]*[=]\?//g'`
			shift
			;;
		-i)
			shift
			IMAGE_ID=$1
			shift
			;;
		--image-id*)
			IMAGE_ID=`echo $1 | sed -e 's/^[^=]*[=]\?//g'`
			shift
			;;
		-H)
			shift
			DOCKER_HOST_ADDR=$1
			shift
			;;
		--host*)
			DOCKER_HOST_ADDR=`echo $1 | sed -e 's/^[^=]*[=]\?//g'`
			shift
			;;				
		*)
			break
			;;
	esac
done

if $UNMOUNT_FLAG
then
	echo "calling unmount"
	if start_unmount
	then
		echo "Successfully unmounted the image from location $MOUNT_PATH"
		exit 0
	else
		echo "Failed to unmount the image from location $MOUNT_PATH"
		exit 1
	fi
fi

if [ -n "$MOUNT_PATH" ] && ([ -n "$IMAGE_ID" ] || [ -n "$TAG_REPO" ])
then
	if docker_version ; then
		if start_mount; then
			echo "Succesfully mounted the image at $MOUNT_PATH"
		else
			echo "Failed to mount the image at $MOUNT_PATH"
			exit 1
		fi
	else
		echo ""
		echo "Couldn't determine the docker version"
		echo "Make sure docker is installed" 
		echo ""
		exit 1
	fi
	exit 0
elif $MOUNT_FLAG 
then
	echo " with mount option you need to provide either IMAGE Id or IMAGE TAG:REPO"
	exit 1
else
	echo ""
	echo "call the $0 with valid options"
	echo ""
	echo "To know the valid options call $0 --help or $0 -h"
	echo ""
	exit 1
fi
