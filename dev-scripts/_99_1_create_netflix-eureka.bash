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
--name=eureka-server \
--package-name=dev.agasen.microsrv.cloud.discovery \
--groupId=dev.agasen.microsrv.cloud.discovery \
--dependencies=cloud-eureka-server \
--version=1.0.0-SNAPSHOT \
eureka-server


## remove gradlew
rm -rf ./eureka-server/gradlew ./eureka-server/gradlew.bat


## add to parent settings.gradle
echo -e "\ninclude ':spring-cloud:eureka-server'" >> "$DIR/../settings.gradle"
