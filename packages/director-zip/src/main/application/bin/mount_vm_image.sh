#!/bin/bash
set -x

## Ret codes
## 0 : Operation successful
## 1 : Invalid input and Usage displayed
## 2 : Image does not exist
## 3 : unmount failed
## 4 : guestmount failed with error code
## 5 : failed to create a new loop device

LOOP_DEVICE=""
NBD_DEVICE=""
MOUNT_OPT="-r"

function free_loop_device() {
	LOOP_DEVICE=$(losetup -f)
	if [ -z "$LOOP_DEVICE" ]
	then
		i=8
		for i in {8..63}
		do
			if [ -e "/dev/loop$i" ]
			then
				continue
			else
				mknod -m 0660 "/dev/loop$i" b $(($i-1)) $i
				if [ $? -eq 0 ]
				then
					LOOP_DEIVCE="/dev/loop$i"
					echo >&2 "successfully created $LOOP_DEVICE"
				else
					echo >&2 "There are no freee loop device"
					echo >&2 "Couldn't create a new loop device"
				fi
				break
			fi
		done
	fi
}

function free_nbd_device() {
	for nbd_dev in /sys/devices/virtual/block/nbd*
	do
		if [ -e ${nbd_dev}/pid ]
		then
			continue
		else
			NBD_DEVICE="/dev/`basename $nbd_dev`"
			break
		fi
	done
}

function unmount_vm_image() {
        echo >&2 "################ Unmounting the mount path###################"
	mnt_volume_path=`mount | grep "$mountPath" | awk '{ print $1}'`
	if [ -n "$mnt_volume_path" ]
	then
		umount $mountPath
		if [ $? -ne 0 ]
		then
			#try once again
			echo >&2 "couldn't unmount in first attempt, will try once again"
			umount $mountPath
		fi
		if [ $? -eq 0 ]
		then
			echo >&2 "unmounted $mnt_volume_path successfully"
			vg_name=`lvs --noheadings $mnt_volume_path 2>/dev/null | awk '{ print $2}'`
			#lvm_vg_name=`echo $vg_name | awk -F/ '{ print $NF}'`
			if [ -n "$vg_name" ]
			then
				vgchange -an $vg_name > /dev/null
				if [ $? -ne 0 ]
				then
					echo >&2 "failed to deactivate logical volumes in volume group $vg_name"
					return 0
				fi
				#pvs | grep "$vg_name" | awk '{ print $1 }'	
				#lvm_vg_name=`echo $output | grep -o "\".*\""`
				echo >&2 "volume group name : '$vg_name'"
				#lvm_vg_name=`echo $lvm_vg_name | awk -F'["]' '{ print $2 }'`
				#echo "volume name with braces removed : $lvm_vg_name"
				device=`pvs --noheadings | grep "$vg_name" | awk '{print $1}'`
				device_uuid=`pvdisplay "$device" | grep "PV UUID" | awk '{ print $3 }'`
				physical_device=`blkid | grep -m 1 "mapper.*$device_uuid" | awk '{ print $1 }'`
				if [ "`echo $physical_device | grep -o "nbd"`" == "nbd" ]
				then
					#nbd_device=`echo "$physical_device" | awk -F'/' '{ print $4 }' | awk -F'p[0-9]' '{ print $1}'`
					local nbd_device=`echo "$physical_device" | awk -F'/' '{ print $4 }' | grep -o "nbd[0-9]\?[0-9]\?"`
					kpartx -d /dev/${nbd_device}
					qemu-nbd -d /dev/${nbd_device}
					if [ $? -eq 0 ]; then
						echo >&2 "successfully removed nbd device $device"
					else
						echo >&2 "failed to remove nbd device $device"
					fi
				elif [ "`echo $physical_device | grep -o "loop"`" == "loop" ]
				then
					loop_device=`echo "$physical_device" | awk -F'/' '{ print $4 }' | grep -o "loop."`
					kpartx -d "/dev/${loop_device}"
					losetup -d "/dev/${loop_device}"
					if [ $? -eq 0 ]; then
                                                echo >&2 "successfully removed loop device $loop_device"
                                        else
                                                echo >&2 "failed to remove loop device $loop_device"
                                        fi

				fi
			elif [ ! -z `echo $mnt_volume_path | grep "nbd"` ]
			then
				#remove the nbd device
				#if [ -n `echo $mnt_volume_path | grep "p[0-9]$"` ]
				#local nbd_device=`echo $mnt_volume_path | awk -F'p[0-9]' '{ print $1}'`
				local nbd_device=`echo $mnt_volume_path | grep -o "/dev/nbd[0-9]\?[0-9]\?"`
				qemu-nbd -d $nbd_device
			fi
			is_vhd_mnt=`ls $vhdMountPath`
			if [ -z "$is_vhd_mnt" ]
			then
				return 0
			fi
			umount $vhdMountPath
			if [ $? -eq 0 ]
			then
				echo >&2 "successfully unmounted $vhdMountPath"
			else
				echo >&2 "cowardly fail to unmount $vhdMountPath"
				echo >&2 "plase try to unmount it manually, and remove the devices such as qemu-nbd or loop manually, if used"
			fi
		else
			echo >&2 "cowardly fail to unmount $mountPath"
			echo >&2 "plase try to unmount it manually, and remove the devices such as qemu-nbd or loop manually, if used"
			return 1
		fi
	fi
}

usage() {
	echo "Usage: used to mount images of , supported format are raw, qcow2, vhd, vdi, vmdk, qcow"
	echo ""
	echo "uses toos like qemu-nbd, kpartx, LVM2, and guestmount, make sure these utilities are pre installed on system"
	echo ""
	echo "       $0 Image-File mount-path 		: to mounting images"
	echo "       $0 Image-File mount-path Unique-Id 	: to mounting images having lvm"
	echo "       $0 mount-path 				: for unmounting images"
}

# usage : 
#	mount_lvm_partition "Physical device name"
function mount_lvm_partition() {
	#find the volume group of the paritition
	DEVICE="$1"
	PVS=`which pvs`
	if [ -z "$PVS" ]
	then
		echo >&2 "LVM utility pvs not found, can't continue"
		echo >&2 "Unabe to mount the $imagePath"
		return 1
	fi
	part_device=`echo "$DEVICE" | awk -F'/' '{print $3}'`
	MAPPED_DEVICE=`blkid | grep "mapper/$part_device" | grep "TYPE=\"LVM2_member\"" | awk -F':' '{ print $1 }'`
	if [ -z $MAPPED_DEVICE ]
	then
		return 1
	fi
        BLOCK_DEVICE=`pvs --noheadings "$MAPPED_DEVICE" | awk '{print $1}'`
	if [ -z "$BLOCK_DEVICE" ]
	then
		echo >&2 "try to get device info once again"
		BLOCK_DEVICE=`pvs --noheadings "$MAPPED_DEVICE" | awk '{print $1}'`
	fi
	DEVICE=$BLOCK_DEVICE
	if [ -z "$DEVICE" ]
	then
		return 1
	fi
	num_of_vg=`pvs |grep -c "$DEVICE"`
	if [ $num_of_vg -ne 1 ]
	then
		echo >&2 "0 more than 1 volume group are present on device"
		echo >&2 "Couldn't mount lvm partition"
		return 1
	fi
	vgname=`pvs | grep "$DEVICE" | awk '{ print $2 }'`
	new_vgname="${vgname}_${lvm_id}"
	vgrename $vgname $new_vgname > /dev/null
	if [ $? -ne 0 ]
	then
		echo >&2 "failed to rename the volume group"
		vgchange -an $vgname
		return 1;
	fi
	sleep 0.05
	#check number of logical volumes in volume group other than swap
	#lvs gives mapping of logical volumes to volume groups
	lvs | grep "$new_vgname" > $LVS_OUT
	local non_swap_fs_count=0
	local lvm_fs_type=""
	local lvm_part_name=""
	while read -r line
	do
		lv_name=`echo $line | awk '{print $1}'`
		local fs_type=`blkid /dev/${new_vgname}/${lv_name} | awk '{ print $3}' | awk -F'["]' '{ print $2}'`
		if [ $fs_type == "swap" ]
		then
			continue
		else
			non_swap_fs_count=$(( $non_swap_fs_count + 1 ))
			lvm_fs_type="$fs_type"
			lvm_part_name="$lv_name"
		fi	
	done < $LVS_OUT
	rm $LVS_OUT
	if [ $non_swap_fs_count -ne 1 ]
	then
		echo >&2 "lvm volume group contains more than one logical volumes"
		vgchange -an $new_vgname
		return 1
	fi
	mount ${MOUNT_OPT} -t $lvm_fs_type /dev/${new_vgname}/${lvm_part_name} $mountPath
	if [ $? -eq 0 ]
	then
		echo >&2 "mounted successfully $imagePath to $mountPath"
		return 0
	else
		echo >&2 "fail to mount lvm partition of $imagePath"
		vgchange -an $new_vgname
		#kpartx -d $DEVICE
		return 1
	fi
}

function mount_raw_image() {
	temp=$(fdisk -l $imagePath | grep -o "Device")
	sector_size=$(fdisk -l $imagePath | grep "Sector size" | awk {'print $4'})

	if [ -z "$temp" ];
	then
		sector=0
	else
       	#fdisk -l $imagePath | grep "$imagePath" | grep -v ":" > $FDISK_OUT
		`fdisk -l $imagePath | grep "$imagePath" | grep -v "$imagePath:" | grep -iv "extended" > $FDISK_OUT`
		cat $FDISK_OUT
		num_of_partition=`grep -c "$imagePath" $FDISK_OUT`
		if [ $num_of_partition -eq 1 ]
		then
			#check the file system type of partition is Linux
			fs_type=`grep "$imagePath" $FDISK_OUT | grep -ic "83[[:space:]]*Linux"`
			if [ $fs_type -eq 1 ]
			then
	        		sector=`cat $FDISK_OUT | awk '{ print $3}'`
			else
				fs_type=`grep "$imagePath" $FDISK_OUT | grep -ic "8e[[:space:]]*Linux LVM"`
				if [ $fs_type -eq 1 ]
				then
					free_loop_device
					if [ -n "$LOOP_DEVICE" ]
					then
						losetup $LOOP_DEVICE $imagePath
						kpartx -av $LOOP_DEVICE > /dev/null
					        if [ $? -ne 0 ]
					        then
					                echo >&2 "Couldn't add table mapping to kernel"
					                return 1
						fi
						sleep 0.05
						mount_lvm_partition $LOOP_DEVICE
						local ret_val=$?
						if [ $ret_val -eq 0 ]
						then
							rm $FDISK_OUT
							return 0
						else
							#remove the device
							kpartx -d $LOOP_DEVICE
							losetup -d $LOOP_DEVICE
						fi
					else
						echo >&2 "Couldn't find loop device"
					fi
				fi
			fi
			#if disk have more than one partition use guestmount
		fi
		rm $FDISK_OUT
	fi
	if [ -n "$sector" ]
	then
		mount ${MOUNT_OPT} -o loop,offset=$(($sector*$sector_size)) $imagePath $mountPath
		if [ $? -eq 0 ]
	    	then
    			echo >&2 "successfully mounted $imagePath to $mountPath"
			return 0
		else
    		    	echo >&2 "Mounting failed"			
			return 1	
	        fi
	fi
	echo >&2 "Will try to mount with guestmount"
        mount_disk_guestmount
}

function mount_qcow2_image() {
        modprobe nbd
	if [ $? -ne 0 ]
	then
		echo >&2 "Will try to mount with guestmount"
		mount_disk_guestmount
		return
	fi

	#find a new free nbd device
	free_nbd_device
	if [ -z "$NBD_DEVICE" ]
	then
		echo >&2 "No nbd device available"
		echo >&2 "Can't mount the image"
		echo >&2 "will try to mount with guestmount"
		mount_disk_guestmount
		return
	fi
	if [ -z "`which qemu-nbd`" ]
	then
		echo >&2 "qemu-nbd binary not present"
		echo >&2 "Will try to mount with guestmount"
		mount_disk_guestmount
		return
	fi
        qemu-nbd -c $NBD_DEVICE $imagePath
        sleep 0.5
	#check for number of partition
	num_of_partition=`blkid | grep -c "${NBD_DEVICE}p\?[0-9]\?:"`
	if [ $num_of_partition -eq 1 ]
	then
		#check if partition is lvm
		local lvm_partition=""
		lvm_partition=`blkid | grep "TYPE=\"LVM.*_member\"" | grep "$NBD_DEVICE"`
		if [ -n "$lvm_partition" ]
		then
			#this partition is lvm
			#local ret_val=$(mount_lvm_partition $NBD_DEVICE)
			kpartx -av $NBD_DEVICE >&2
			sleep 0.05
			mount_lvm_partition $NBD_DEVICE
			local ret_val=$?
			if [ $ret_val -eq 0 ]
			then
				return 0
			fi
			echo >&2 "Can't mount lvm parition"
			kpartx -d $NBD_DEVICE
			sleep 0.05
			qemu-nbd -d $NBD_DEVICE
		else
			#mount using qemu-nbd
        		if [ -b ${NBD_DEVICE}p1 ]
	        	then
	                	mount ${MOUNT_OPT} ${NBD_DEVICE}p1 $mountPath
        		else
                		mount ${MOUNT_OPT} $NBD_DEVICE $mountPath
		        fi
			if [ $? -eq 0 ]
			then
				echo >&2 "Mounted successfully $imagePath to $mountPath"
				return 0
			else
				echo >&2 "Mounting with qemu-nbd failed"
				qemu-nbd -d $NBD_DEVICE
			fi
		fi
	else
		#mount using guestmount
		qemu-nbd -d $NBD_DEVICE
	fi
	echo >&2 "will try to mount with guestmount"
	mount_disk_guestmount
}

function mount_vhd_image() {
	if [ -z "`which vdfuse`" ]
	then
		echo >&2 "cant mountt disk $imagePath with vdfuse"
		echo >&2 "will try to mount with qemu-nbd"
		mount_qcow2_image
		return
	fi
        vdfuse -w -f $imagePath $vhdMountPath
	#sleep 1
	if [ $? -eq 0 ]
	then
	        num_of_partition=$(ls $vhdMountPath | grep -c "Partition")

        	if [ $num_of_partition -eq 0 ]
	        then
        	        imagePath="$vhdMountPath/EntireDisk"
			main
	        elif [ $num_of_partition -eq 1 ]
		then
                	imagePath="$vhdMountPath/Partition1"
			main
		else
			echo >&2 "$imagePath contains more than one partition"
			umount $vhdMountPath
		fi
        fi
	echo >&2 "cant mountt disk $imagePath with vdfuse"
	echo >&2 "will try to mount with guestmount"
	mount_disk_guestmount
}

function mount_disk_guestmount()
{
	imagePath=$(readlink -f $imagePath)
	guestMountBinary=`which guestmount`
	if [ "$guestMountBinary" == "" ] ; then
		echo >&2 "guestmount binary not found, please install libguestfs"
		echo >&2 "and libguestfs-tools ( or its corresponding packages as per"
		echo >&2 "your linux flavour)"
		exit 1
	fi
	## Proceed mounting with guestmount
	export LIBGUESTFS_BACKEND=direct
	time $guestMountBinary -a $imagePath -i --ro $mountPath
	retcode=$?
	if [ $retcode -eq 0 ] ; then
		echo >&2 "Mounted the disk image successfully"
	else
		echo >&2 "Guestmount failed and returned with exit code $retcode"
		exit 4
	fi
}

function main() {

	imageFormat=$(qemu-img info $imagePath | grep "file format" | awk -F ':' '{ print $2 }' | sed -e 's/ //g')

	echo "Disk image format : $imageFormat"

	case "$imageFormat" in
	   "raw")
		    echo "Mounting the raw Image."
		    #check_unmount_status
		    mount_raw_image
		    #mount_raw_image_duplicate
		    if [ $? ]
		    then
		            echo "Successfully mounted raw image, exiting ..."
		            exit 0
		    else
		            echo "Error in mounting the image"
		            exit 1
		    fi
	   ;;
	   "vpc")
		    echo "########### Mounting vhd Image" 
		    #check_unmount_status
		    mount_vhd_image
		    #mount_qcow2_image
		    if [ $? ];
		    then
		            echo "Successfully mounted vhd image, exiting ..."
		            exit 0
		    else
		            echo "Error in mounting the image"
		            exit 1
		    fi
	   ;;
	   "vdi")
		    echo "########### Mounting vdi Image" 
		    #check_unmount_status
		    mount_vhd_image
		    #mount_qcow2_image
		    if [ $? ];
		    then
		            echo "Successfully mounted vdi image, exiting ..."
		            exit 0
		    else
		            echo "Error in mounting the image"
		            exit 1
		    fi
	   ;;
	   "vmdk")
		    echo "########### Mounting vmdk Image" 
		    #check_unmount_status
		    mount_vhd_image
		    #mount_qcow2_image
		    if [ $? ];
		    then
		            echo "Successfully mounted vmdk image, exiting ..."
		            exit 0
		    else
		            echo "Error in mounting the image"
		            exit 1
		    fi
	   ;;
	   "qcow2")
		    echo "################ Mounting qcow2 Image." 
		    #check_unmount_status
		    mount_qcow2_image
		    #mount_disk_guestmount
		    if [ $? -eq 0 ]
		    then
		            echo "Successfully mounted qcow2 image, exiting ..."
		            exit 0
		    else
		            echo "Error in mounting the image"
		            exit 1
		    fi
	   ;;
	   *)
		    echo "############### format other than vhd, raw, qcow2 using guestmount"
		    # mount_qcow2_image
		    mount_disk_guestmount
		    if [ $? ]
		    then
		            echo "Successfully mounted qcow2 image, exiting ..."
		            exit 0
		    else
		            echo "Error in mounting the image"
		            exit 1
		    fi
	esac
}
#mount_disk_guestmount

mount_check=""
if [ $# -lt 1 ] || [ $# -gt 3 ]
then
	usage
	exit 1
elif [ $# -eq 2 ] || [ $# -eq 3 ]
then
	imagePath=$1
	FDISK_OUT="$2/fdisk.out"
	LVS_OUT="$2/lvs.out"
	mountPath="$2/mount"
	vhdMountPath="$2/vhdmnt"
	if [ $# -eq 3 ]
	then
		lvm_id="$3"
	fi
	if [ ! -e $imagePath ] ; then
        	echo "Image $imagePath does not exist..."
	        exit 2
	fi
	mount_check=`mount | grep $mountPath`
	if [ -n "$mount_check" ]
	then
		echo "$mountPath is already mounted"
		unmount_vm_image
	fi
	mkdir -p $mountPath
	mkdir -p $vhdMountPath
	main
else
	if [ "$1" == "--help" ] || [ "$1" == "-h" ]
	then
		usage
		exit 0
	fi
	mountPath="$1/mount"
	vhdMountPath="$1/vhdmnt"
	mount_check=`mount | grep $mountPath`
	if [ -z "$mount_check" ]
	then
		echo "path $mountPath is not mounted"
		exit 0
	fi
	unmount_vm_image
	rm -rf `dirname $mountPath`
fi
exit 0
