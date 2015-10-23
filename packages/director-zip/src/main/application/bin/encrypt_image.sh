
usage() {
	echo "Usage: $0 Image-File image-path : patch to the image to be encrypted"
	echo "       $0 Image-File image-encrypt-path  : path to enc image"	
	echo "       $0 Image-File password  : password for encryption"	
	exit 1
}

if [ $# -lt 1 ] || [ $# -gt 3 ]
then
	usage
elif [ $# -eq 3 ] 
then
	imagePath=$1
	encPath=$2
	password=$3
	
	enc_check=`openssl enc -aes-128-ofb -in $imagePath -out $encPath -pass pass:$password`
	
	if [ -n "$enc_check" ]
	then
		echo "Encryption failed"		
		exit 1
	fi	
	
fi
exit 0
