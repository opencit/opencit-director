#!/bin/bash

function inject_policy_and_save_image(){
	echo "Running Image..!!"
	foo="$tag"
	foo+="_source"
	docker run "$repository:$foo" mkdir -p /trust
	containerId=`docker ps -aq --no-trunc | awk '{print $1; exit}'`
	echo "Container ID is :: $containerId"
	echo "Injecting Policy..!!"
	docker cp "$trustPolicyPath/trustpolicy.xml" "$containerId:/trust/trustpolicy.xml"
	echo "Injecting Completed..!!"
	echo "Committing Image..!!"
	imageId=`docker commit $containerId "$repository:$tag"`
	echo "New Image ID is :: $imageId"
	echo "Image Committed Successfully..!!"
	echo "Creating Tar..!!"
	docker save -o "$trustPolicyPath/$repository:$tag" "$repository:$tag"
	echo "Tar Created Successfully..!!"
	echo "Removing Container..!!"
	docker rm $containerId
	echo "Removing Image..!!"
	docker rmi -f "$repository:$tag"
}

if [ $# -ne 3 ]
then
   echo "Insufficient Parameters"
   exit 1
fi

repository=$1
tag=$2
trustPolicyPath=$3

if [ ! -z $repository ] || [ ! -z $tag ] || [ ! -z $trustPolicyPath ]
then
    echo "Repository is: $repository"
    echo "Tag is: $tag"
    echo "TrustPolicyPath is: $trustPolicyPath"
    inject_policy_and_save_image
    echo "Injecting and save successfully"
else
    echo "Please provide a valid repository, tag, trustPolicyPath"
    exit 1
fi
