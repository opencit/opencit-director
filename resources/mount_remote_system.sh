#!/bin/bash
set -x
mountPath="/tmp/mount"
fileSystemPath="/"

function mount_remote_file_system(){
   echo "####Mount the Remote File System"

      echo "Mount Path exists: $mountPathCheck"
      #Mount a remote system
      #echo P@ssw0rd  | sshfs -o password_stdin root@10.1.68.118:/ /tmp/mount
      echo $password | sshfs -o password_stdin $userName@$ipAddress:$fileSystemPath $mountPath
      echo "Mount Remote System result was $?"
}

function unmount_remote_file_system(){
   echo "Unmount the Remote file system"
   mountPathCheck=$(mount | grep -o "$mountPath")
   echo "Print MountPathCheck $mountPathCheck"
    if [ ! -z $mountPathCheck ]
    then
       echo "Unmount the file system"
       umount $mountPath
       echo "Un mount operation result: $?"
    fi
}


if [ $# -eq 0 ]
then
    echo "Unmount the remote File System"
    unmount_remote_file_system
    exit 0
fi

ipAddress=$1
userName=$2
password=$3

if [ ! -z ipAddress ] || [ ! -z userName ] || [ ! -z password ]
then
    echo " IP address is: $ipAddress"
    echo "Username is: $userName"
    echo "Password is: $password"
    #Check mount Status
    unmount_remote_file_system
    echo "Unmount was done for remote directory"
    mount_remote_file_system
    echo "Result of mounT was $?"
    if [ $? -ne 0 ]
    then
        echo "Error in mounting the remote File System"
        exit 1
    else
        echo "Remote fi;le systtem eas mounted successfully"
        exit 0
    fi
else
   echo " Please provide a valid IP, user name and Password"
fi
