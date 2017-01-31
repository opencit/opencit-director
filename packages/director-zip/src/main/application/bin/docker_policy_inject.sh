#!/bin/bash

function inject_policy(){
	echo "Running Image..!!"
	containerId=`docker run -id "$repository:$tag"`
	docker exec $containerId mkdir -p /trust
	docker stop $containerId
#	containerId=`docker ps -aq --no-trunc | awk '{print $1; exit}'`
	echo "Container ID is :: $containerId"
	echo "Injecting Policy..!!"
	docker cp "$trustPolicyPath/trustpolicy.xml" "$containerId:/trust/trustpolicy.xml"
	echo "Injecting Completed..!!"
	echo "Committing Image..!!"
	imageId=`docker commit $containerId "$newrepository:$newtag"`
	echo "New Image ID is :: $imageId"
	echo "Image Committed Successfully..!!"
	echo "Removing Container..!!"
	docker rm $containerId
}

if [ $# -ne 5 ]
then
   echo "Insufficient Parameters"
   exit 1
fi

repository=$1
tag=$2
newrepository=$3
newtag=$4
trustPolicyPath=$5

if [ ! -z $repository ] || [ ! -z $tag ] || [ ! -z $trustPolicyPath ] || [ ! -z $newrepository ] || [ ! -z $newtag ]
then
    echo "Repository is: $repository"
    echo "Tag is: $tag"
	echo "New repository is: $newrepository"
    echo "New tag is: $newtag"
    echo "TrustPolicyPath is: $trustPolicyPath"
    inject_policy
    echo "Injecting and save successfully"
else
    echo "Please provide a valid repository, tag, trustPolicyPath"
    exit 1
fi
