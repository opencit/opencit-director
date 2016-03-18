#!/bin/bash
set -x

function mount_win_host(){

	if [ "$domain" == "" ] || [ "$domain" == "null" ]
	then
		mount -t cifs //$ipAddress/$partition\$ $mountpath -o user=$userName,pass=$password,dir_mode=$dir_mode,file_mode=$file_mode
	else
    		mount -t cifs //$ipAddress/$partition\$ $mountpath -o dom=$domain,user=$userName,pass=$password,dir_mode=$dir_mode,file_mode=$file_mode
	fi

}

if [ $# -ne 8 ]
then
   echo "Insufficient Parameters"
   exit 1
fi

ipAddress=$1
partition=$2
mountpath=$3
userName=$4
password=$5
domain=$8
file_mode=$6
dir_mode=$7
if [ ! -z $ipAddress ] || [ ! -z $partition ] || [ ! -z $mountpath ] || [ ! -z $userName ] || [ ! -z $password ] || [ ! -z $domain ] || [ ! -z $file_mode ] || [ ! -z $dir_mode ]
then
    echo "ipAddress is: $ipAddress"
    echo "partition is: $partition"
    echo "mountpath is: $mountpath"
    echo "userName is: $userName"
    echo "password is: $password"
	echo "domain is: $domain"
    echo "File Mode is: $file_mode"
        echo "domain is: $dir_mode"

    mount_win_host
else
    exit 1
fi
