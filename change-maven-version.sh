#!/bin/bash

# define action usage commands
usage() { echo "Usage: $0 [-v \"version\"]" >&2; exit 1; }

# set option arguments to variables and echo usage on failures
version=
while getopts ":v:" o; do
  case "${o}" in
    v)
      version="${OPTARG}"
      ;;
    \?)
      echo "Invalid option: -$OPTARG"
      usage
      ;;
    *)
      usage
      ;;
  esac
done

if [ -z "$version" ]; then
  echo "Version not specified" >&2
  exit 2
fi

changeVersionCommand="mvn versions:set -DnewVersion=${version}"
changeParentVersionCommand="mvn versions:update-parent -DallowSnapshots=true -DparentVersion=${version}"
mvnInstallCommand="mvn clean install"

(cd maven/director-maven-root && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"maven/director-maven-root\" folder" >&2; exit 3; fi
(cd maven/director-maven-root && $mvnInstallCommand)
if [ $? -ne 0 ]; then echo "Failed to maven install \"maven/director-maven-root\"" >&2; exit 3; fi
(cd maven && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"maven\" folder" >&2; exit 3; fi
ant ready
if [ $? -ne 0 ]; then echo "Failed to run \"ant ready\" command" >&2; exit 3; fi
$changeVersionCommand
if [ $? -ne 0 ]; then echo "Failed to change maven version at top level" >&2; exit 3; fi
$changeParentVersionCommand
if [ $? -ne 0 ]; then echo "Failed to change maven parent versions" >&2; exit 3; fi
sed -i 's/\(<version>\).*\(<\/version>\)/\1'${version}'\2/g' features/director-glance/feature.xml
if [ $? -ne 0 ]; then echo "Failed to change version in \"features/director-glance/feature.xml\"" >&2; exit 3; fi
sed -i 's/\(<version>\).*\(<\/version>\)/\1'${version}'\2/g' features/director-html5/feature.xml
if [ $? -ne 0 ]; then echo "Failed to change version in \"features/director-html5/feature.xml\"" >&2; exit 3; fi
sed -i 's/\(<version>\).*\(<\/version>\)/\1'${version}'\2/g' features/director-images-api/feature.xml
if [ $? -ne 0 ]; then echo "Failed to change version in \"features/director-images-api/feature.xml\"" >&2; exit 3; fi
sed -i 's/\(<version>\).*\(<\/version>\)/\1'${version}'\2/g' features/director-images-ws/feature.xml
if [ $? -ne 0 ]; then echo "Failed to change version in \"features/director-images-ws/feature.xml\"" >&2; exit 3; fi
sed -i 's/\(<version>\).*\(<\/version>\)/\1'${version}'\2/g' features/director-persistence/feature.xml
if [ $? -ne 0 ]; then echo "Failed to change version in \"features/director-persistence/feature.xml\"" >&2; exit 3; fi
sed -i 's/\(<version>\).*\(<\/version>\)/\1'${version}'\2/g' features/director-async-image-processor/feature.xml
if [ $? -ne 0 ]; then echo "Failed to change version in \"features/director-async-image-processor/feature.xml\"" >&2; exit 3; fi
sed -i 's/\(<version>\).*\(<\/version>\)/\1'${version}'\2/g' features/director-dockerhub/feature.xml
if [ $? -ne 0 ]; then echo "Failed to change version in \"features/director-dockerhub/feature.xml\"" >&2; exit 3; fi
sed -i 's/\(<version>\).*\(<\/version>\)/\1'${version}'\2/g' features/director-image-store/feature.xml
if [ $? -ne 0 ]; then echo "Failed to change version in \"features/director-image-store/feature.xml\"" >&2; exit 3; fi
sed -i 's/\(<version>\).*\(<\/version>\)/\1'${version}'\2/g' features/director-kms/feature.xml
if [ $? -ne 0 ]; then echo "Failed to change version in \"features/director-kms/feature.xml\"" >&2; exit 3; fi




(cd packages  && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"packages\" folder" >&2; exit 3; fi
(cd packages  && $changeParentVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven parent versions in \"packages\" folder" >&2; exit 3; fi

sed -i 's/\-[0-9\.]*\(\-SNAPSHOT\|\(\-\|\.zip$\|\.bin$\|\.jar$\)\)/-'${version}'\2/g' build.targets
if [ $? -ne 0 ]; then echo "Failed to change versions in \"build.targets\" file" >&2; exit 3; fi
