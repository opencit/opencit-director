function mount_win_host(){

	mount -t cifs //$ipAddress/$partition\$ $mountpath -o user=$userName,pass=$password,dir_mode=0444,file_mode=0444

}

if [ $# -ne 5 ]
then
   echo "Insufficient Parameters"
   exit 1
fi

ipAddress=$1
partition=$2
mountpath=$3
userName=$4
password=$5


if [ ! -z $ipAddress ] || [ ! -z $partition ] || [ ! -z $mountpath ] || [ ! -z $userName ] || [ ! -z $password ]
then
    echo "ipAddress is: $ipAddress"
    echo "partition is: $partition"
    echo "mountpath is: $mountpath"
    echo "userName is: $userName"
    echo "password is: $password"
    mount_win_host
else
    exit 1
fi
