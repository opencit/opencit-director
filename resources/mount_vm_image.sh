#!/bin/bash
set -x
mountPath="/tmp/mount"
vhdMountPath="/tmp/vhdmnt"

function unmount_vm_image() {
        echo "################ Unmounting the mount path"
        mountPathCheck=$(mount | grep -o "$mountPath")
        if [ ! -z $mountPathCheck ]
        then
                umount $mountPath 2>/dev/null
		if [ ! $? ]
		then
			exit 1
		fi	
        fi

        kpartx -d /dev/loop0
        losetup -d /dev/loop0
        qemu-nbd -d /dev/nbd0

        vhdPathCheck=$(mount | grep -o "$vhdMountPath")
        if [ ! -z $vhdPathCheck ]
        then
                umount $vhdMountPath 2>/dev/null
                if [ ! $? ]
                then
                        echo "Unmounting $vhdMountPath Failed . . ."
                        exit 1
                fi
        fi
	
	killall -9 qemu-nbd
	killall -9 vdfuse

        rm -rf $mountPath
        mkdir -p $mountPath
        rm -rf $vhdMountPath
        mkdir $vhdMountPath
}

function mount_qcow2_image() {
        modprobe nbd
        qemu-nbd -d /dev/nbd0
	#echo "############ Image file path is $imagePath"
        qemu-nbd -c /dev/nbd0 $imagePath
	sleep 2
        if [ -b /dev/nbd0p1 ]
        then
                mount /dev/nbd0p1 $mountPath
        else
                mount /dev/nbd0 $mountPath
        fi
}

function mount_raw_image() {
        temp=$(fdisk -l $imagePath | grep -o "Device")

        #Check for availability of partions in the image
        #echo "##################### ******************  $temp"
        if [ -z $temp ];
        then
                #echo "############## In main If"
                sector="0"
        else
                #Check for value of 'boot' field in the output to decide the value of 'start' field whether $2 or $3
                temp=$(fdisk -l $imagePath | grep $imagePath*'1' | grep -v ":" | awk '{ print $7 }')
                #echo "Value of temp is $temp"
                if [ -z $temp ];
                then
                        #echo "################ In the nested if"
                        sector=$(fdisk -l $imagePath | grep $imagePath*'1' | grep -v ":" | awk '{ print $2 }')
                else
                        #echo "######################### in the nested else"
                        sector=$(fdisk -l $imagePath | grep $imagePath*'1' | grep -v ":" | awk '{ print $3 }')
                fi
        fi
        #echo "################ sector start is $sector"
        mount -o loop,offset=$(($sector*512)) $imagePath $mountPath
}

function mount_raw_image_duplicate() {
	#loopDevice=$(losetup -f)
	loopDevice="/dev/loop0"
	kpartx -d $loopDevice
	losetup -d $loopDevice
	losetup $loopDevice $imagePath
	out=$(kpartx -av $loopDevice | grep -o "$loopDevice")
	#echo "#####%%%%%%%#######__________$out___________################"
	if [ -z $out ]
	then
		mount $loopDevice $mountPath
	else
		partition=$(echo $loopDevice | awk -F '/' '{ print $3 }')p'1'
		mount /dev/mapper/$partition $mountPath
	fi
}

function mount_vhd_image() {
        vdfuse -w -f $imagePath $vhdMountPath
	sleep 1
        out=$(ls $vhdMountPath | grep "Partition1")

        if [ -z $out ];
        then
                #mount $vhdMountPath/EntireDisk $mountPath
		imagePath="$vhdMountPath/EntireDisk"
		#mount_raw_image
		mount_raw_image_duplicate
        else
                #mount $vhdMountPath/Partition1 $mountPath
		imagePath="$vhdMountPath/Partition1"
		#mount_raw_image
		mount_raw_image_duplicate
        fi
}

usage(){
        echo "Usage: $0 Image-File"
        exit 1
}

function check_unmount_status() 
{
	if ( unmount_vm_image )
	then
		echo "Successfully unmounted "
	else
		echo "Error while unmounting "
		exit 1
	fi
}

if [ $# -eq 0 ]
then
	#check_unmount_status
	unmount_vm_image
	exit 0
fi

imagePath=$1

checkVhd=$(tar tf $imagePath 2>/dev/null | grep 0.vhd)
if [ ! -z $checkVhd ]
then
        parentDir=$(dirname $imagePath)
	#echo "---------------- In the vhd check"
        cd $parentDir
        tar zxf $imagePath
        imagePath=$(readlink -f 0.vhd)	
else
	echo ""
fi

imageFormat=$(qemu-img info $imagePath  | grep "file format" | awk -F ':' '{ print $2 }' | sed -e 's/ //g')

echo "################ Original image format is $imageFormat"

case "$imageFormat" in
   "raw")
	echo "Mounting the raw Image."
	#check_unmount_status
	unmount_vm_image
	#mount_raw_image
	mount_raw_image_duplicate
   ;;
   "vpc")
	echo "########### Mounting vhd Image" 
	#check_unmount_status
	unmount_vm_image
	mount_vhd_image
   ;;
   "qcow2")
	echo "################ Mounting qcow2 Image." 
	#check_unmount_status
	unmount_vm_image
	mount_qcow2_image
   ;;
   *)
	echo "############### format other than vhd, raw, qcow2"
	exit 1
esac
if [ $? -ne 0 ]
then
	echo "Error in mounting the image"
        exit 1
else
	echo "Successfully mounted image"
        exit 0
fi


