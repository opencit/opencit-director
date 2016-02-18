#!/bin/bash

function push_docker_image_to_registry(){
	echo "Login Begins..!!"
	echo docker login -u $username -p $password -e $email $registry
	docker login -u $username -p $password -e $email $registry
	echo "Setup For Pushing Image..!!"
	docker tag "$repository:$tag" "$username/$repository:$tag"
	docker push "$username/$repository:$tag"
	docker rmi -f "$username/$repository:$tag"
	docker logout
}

if [ $# -ne 6 ]
then
   echo "Insufficient Parameters"
   exit 1
fi

registry=$1
username=$2
password=$3
email=$4
repository=$5
tag=$6

if [ ! -z $registry ] || [ ! -z $username ] || [ ! -z $password ] || [ ! -z $email ] || [ ! -z $repository ] || [ ! -z $tag ]
then
    	echo "Registry is: $registry"
    	echo "Username is: $username"
	echo "Password is: $password"
    	echo "Email is: $email"

    	echo "Repository is: $repository"
    	echo "Tag is: $tag"
    	push_docker_image_to_registry
    	echo "Pushed Successfully..!!!"
else
    	echo "Please provide a valid registry, username, password, email, repository, tag"
    	exit 1
fi
