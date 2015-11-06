#!/bin/bash
set -x
fileSystemPath="/"

function mount_remote_file_system(){
   echo "####Mount the Remote File System"
 umount $mountPath
      echo "Mount Path exists: $mountPathCheck"
      #Mount a remote system
      #echo P@ssw0rd  | sshfs -o password_stdin root@10.1.68.118:/ /tmp/mount
      echo $password | sshfs -o reconnect -o password_stdin $userName@$ipAddress:$fileSystemPath $mountPath
    if [ $? -ne 0 ]
    then
        echo "Error in mounting the remote File System"

        exit 1
    else
        echo "Remote file systtem was mounted successfully"
        exit 0
    fi
}

function unmount_remote_file_system(){
   echo "Unmount the Remote file system"
   mountPathCheck=$(mount | grep -o "$mountPath")
   echo "Print MountPathCheck $mountPathCheck"
    if [ ! -z $mountPathCheck ]
    then
       echo "Unmount the file system"
       umount $mountPath
       echo "Unmount operation result: $?"
       rm -rf $mountPath
    fi
}


if [ $# -eq 1 ]
then
    echo "Unmount the remote File System"
    mountPath=$1
    unmount_remote_file_system
    exit 0
fi

ipAddress=$1
userName=$2
password=$3
mountPath=$4

if [ ! -z $ipAddress ] || [ ! -z $userName ] || [ ! -z $password ] || [! -z $mountPath]
then
    echo " IP address is: $ipAddress"
    echo "Username is: $userName"
    echo "Password is: $password"
    #Check mount Status
    unmount_remote_file_system
    echo "Unmount was done for remote directory"
    if ! [ -d $mountPath ]; then
        mkdir -p $mountPath
        if [ $? -ne 0 ]; then
            exit 1
        fi
    fi
    mount_remote_file_system    
else
    echo "Please provide a valid IP, user name, Password and mount path"
    exit 1
fi
