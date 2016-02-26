#!/bin/bash

function push_docker_image_to_registry(){
	echo "Login Begins..!!"
	docker login -u $username -p $password -e $email
	echo "Setup For Pushing Image..!!"
	docker tag "$repository:$tag" "$username/$repository:$tag"
	docker rmi -f "$repository:$tag"
	docker push "$username/$repository:$tag"
	docker rmi -f "$username/$repository:$tag"
	docker logout
}

if [ $# -ne 5 ]
then
   echo "Insufficient Parameters"
   exit 1
fi

username=$1
password=$2
email=$3
repository=$4
tag=$5

if [ ! -z $username ] || [ ! -z $password ] || [ ! -z $email ] || [ ! -z $repository ] || [ ! -z $tag ]
then
    echo "Username is: $username"
	echo "Password is: $password"
    echo "Email is: $email"
    echo "Repository is: $repository"
    echo "Tag is: $tag"
    push_docker_image_to_registry
    echo "Pushed Successfully..!!!"
else
    echo "Please provide a valid username, password, email, repository, tag"
    exit 1
fi
