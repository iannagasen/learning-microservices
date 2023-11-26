#! /bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$DIR"
cd ../

mkdir -p "./spring-cloud" && echo "Folder spring-cloud created"
cd "./spring-cloud"
echo $(pwd)

spring init \
--boot-version=3.1.0 \
--type=gradle-project \
--java-version=17 \
--packaging=jar \
--name=gateway \
--package-name=dev.agasen.microsrv.cloud.gateway \
--groupId=dev.agasen.microsrv.cloud.gateway \
--dependencies=cloud-gateway \
--version=1.0.0-SNAPSHOT \
gateway


## remove gradlew
rm -rf ./gateway/gradlew ./gateway/gradlew.bat


## add to parent settings.gradle
echo -e "\ninclude ':spring-cloud:gateway'" >> "$DIR/../settings.gradle"
