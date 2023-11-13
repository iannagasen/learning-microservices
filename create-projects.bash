#!/bin/bash

# clean up on failure
CURRENT_DIR=$(pwd)
WORKING_DIR=microservices

function cleanup() {
  echo "has errors"
  rm -rf "${CURRENT_DIR_DIR}/${WORKING_DIR}/"
}
trap cleanup EXIT

# exit immediately if any command failed
set -e

mkdir "${WORKING_DIR}/"
cd "${WORKING_DIR}/"

PRODUCT_SERVICE=product-service
RECOMMENDATION_SERVICE=recommendation-service
REVIEW_SERVICE=review-service
PRODUCT_COMPOSITE_SERVICE=product-composite-service
ROOT_PKG=dev.agasen.microsrv.core

MAPSTRUCT_GRADLE_DEP="
ext {
    mapstructVersion = \"1.5.3.Final\"
}

dependencies {
  implementation \"org.mapstruct:mapstruct:\${mapstructVersion}\"
  compileOnly \"org.mapstruct:mapstruct-processor:\${mapstructVersion}\"
  annotationProcessor \"org.mapstruct:mapstruct-processor:\${mapstructVersion}\"
  testAnnotationProcessor \"org.mapstruct:mapstruct-processor:\${mapstructVersion}\"
}"
TESTCONTAINER_MONGODB_GRADLE_DEP="
dependencies {
  implementation platform('org.testcontainers:testcontainers-bom:1.17.6')
  testImplementation 'org.testcontainers:testcontainers'
  testImplementation 'org.testcontainers:junit-jupiter'
  testImplementation 'org.testcontainers:mongodb'
}"
TESTCONTAINER_MYSQL_GRADLE_DEP="
dependencies {
  implementation platform('org.testcontainers:testcontainers-bom:1.17.6')
  testImplementation 'org.testcontainers:testcontainers'
  testImplementation 'org.testcontainers:junit-jupiter'
  testImplementation 'org.testcontainers:mysql'
}"
FALSE_JAR_GRADLE="
jar {
    enabled = false
}"


echo "STARTING...."

##### MICROSERVICES ################################### 
## PRODUCT SERVICE ####################################

spring init \
--boot-version=3.0.4 \
--type=gradle-project \
--java-version=17 \
--packaging=jar \
--name="$PRODUCT_SERVICE" \
--package-name="${ROOT_PKG}.product" \
--groupId="${ROOT_PKG}.product" \
--dependencies=actuator,webflux,data-mongodb,lombok \
--version=1.0.0-SNAPSHOT \
"${PRODUCT_SERVICE}"

cd "${PRODUCT_SERVICE}"

echo "$MAPSTRUCT_GRADLE_DEP" >> build.gradle
echo "$TESTCONTAINER_MONGODB_GRADLE_DEP" >> build.gradle
echo "$FALSE_JAR_GRADLE" >> build.gradle

./gradlew build \
&& echo "$PRODUCT_SERVICE build successfully" \
|| echo "$PRODUCT_SERVICE build failed"

cd ../

########################################################
## RECOMMENDATION SERVICE ##############################
spring init \
--boot-version=3.0.4 \
--type=gradle-project \
--java-version=17 \
--packaging=jar \
--name="$RECOMMENDATION_SERVICE" \
--package-name="${ROOT_PKG}.recommendation" \
--groupId="${ROOT_PKG}.recommendation" \
--dependencies=actuator,webflux,data-mongodb,lombok \
--version=1.0.0-SNAPSHOT \
"${RECOMMENDATION_SERVICE}"

cd "$RECOMMENDATION_SERVICE"

echo "$MAPSTRUCT_GRADLE_DEP" >> build.gradle
echo "$TESTCONTAINER_MONGODB_GRADLE_DEP" >> build.gradle
echo "$FALSE_JAR_GRADLE" >> build.gradle

./gradlew build \
&& echo "$RECOMMENDATION_SERVICE build successfully" \
|| echo "$RECOMMENDATION_SERVICE build failed"

cd ../

########################################################
## REVIEW SERVICE ######################################
spring init \
--boot-version=3.0.4 \
--type=gradle-project \
--java-version=17 \
--packaging=jar \
--name="$REVIEW_SERVICE" \
--package-name="${ROOT_PKG}.review" \
--groupId="${ROOT_PKG}.review" \
--dependencies=actuator,webflux,mysql,lombok \
--version=1.0.0-SNAPSHOT \
"${REVIEW_SERVICE}"

cd "$REVIEW_SERVICE"

echo "$MAPSTRUCT_GRADLE_DEP" >> build.gradle
echo "$TESTCONTAINER_MYSQL_GRADLE_DEP" >> build.gradle
echo "$FALSE_JAR_GRADLE" >> build.gradle

./gradlew build \
&& echo "$REVIEW_SERVICE build successfully" \
|| echo "$REVIEW_SERVICE build failed"

cd ../

##########################################################
## PRODUCT COMPOSITE SERVICE #############################
spring init \
--boot-version=3.0.4 \
--type=gradle-project \
--java-version=17 \
--packaging=jar \
--name="$PRODUCT_COMPOSITE_SERVICE" \
--package-name="${ROOT_PKG}.composite.product" \
--groupId="${ROOT_PKG}.composite.product" \
--dependencies=actuator,webflux,lombok \
--version=1.0.0-SNAPSHOT \
"${PRODUCT_COMPOSITE_SERVICE}"

./gradlew build \
&& echo "$REVIEW_SERVICE build successfully" \
|| echo "$REVIEW_SERVICE build failed"

cd "${CURRENT_DIR}"

## end of core microservices #############################
##########################################################


##########################################################
## get api and util ######################################
DEP_DIR=deps
mkdir "$DEP_DIR"
cd "$DEP_DIR"

git clone -b main https://github.com/PacktPublishing/Microservices-with-Spring-Boot-and-Spring-Cloud-Third-Edition.git
mv ./Microservices-with-Spring-Boot-and-Spring-Cloud-Third-Edition/Chapter03/2-basic-rest-services/api "${CURRENT_DIR}/"
mv ./Microservices-with-Spring-Boot-and-Spring-Cloud-Third-Edition/Chapter03/2-basic-rest-services/util "${CURRENT_DIR}/"

cd "$CURRENT_DIR"
rm -R "$DEP_DIR"
##########################################################


##########################################################
## making the project MULTI-MODULE project ###############
# 1. Add api and util as dependency to all core microservices
echo "CONFIGURING api AND util DEPENDENCIES .........."
API_UTIL_DEPS="
dependencies {
  implementation project(':api') 
  implementation project(':util')
}
"
echo "$API_UTIL_DEPS" >> "${CURRENT_DIR}/${WORKING_DIR}/${PRODUCT_SERVICE}/build.gradle"
echo "$API_UTIL_DEPS" >> "${CURRENT_DIR}/${WORKING_DIR}/${RECOMMENDATION_SERVICE}/build.gradle"
echo "$API_UTIL_DEPS" >> "${CURRENT_DIR}/${WORKING_DIR}/${REVIEW_SERVICE}/build.gradle"
echo "$API_UTIL_DEPS" >> "${CURRENT_DIR}/${WORKING_DIR}/${PRODUCT_COMPOSITE_SERVICE}/build.gradle"

# 2. Copy 1 gradle/w to root directory
cp -r "${CURRENT_DIR}/${WORKING_DIR}/${PRODUCT_SERVICE}/gradle" "$CURRENT_DIR"
cp "${CURRENT_DIR}/${WORKING_DIR}/${PRODUCT_SERVICE}/gradlew" "$CURRENT_DIR"
cp "${CURRENT_DIR}/${WORKING_DIR}/${PRODUCT_SERVICE}/gradlew.bat" "$CURRENT_DIR"
cp "${CURRENT_DIR}/${WORKING_DIR}/${PRODUCT_SERVICE}/.gitignore" "$CURRENT_DIR"

# 3. Remove gradle/w for each microservices
echo "Cleaning gradle files for each microservices ......."
rm -rfv "${CURRENT_DIR}/${WORKING_DIR}/${PRODUCT_SERVICE}/gradle"
rm -fv "${CURRENT_DIR}/${WORKING_DIR}/${PRODUCT_SERVICE}/gradlew"
rm -fv "${CURRENT_DIR}/${WORKING_DIR}/${PRODUCT_SERVICE}/gradlew.bat"

rm -rfv "${CURRENT_DIR}/${WORKING_DIR}/${RECOMMENDATION_SERVICE}/gradle"
rm -fv "${CURRENT_DIR}/${WORKING_DIR}/${RECOMMENDATION_SERVICE}/gradlew"
rm -fv "${CURRENT_DIR}/${WORKING_DIR}/${RECOMMENDATION_SERVICE}/gradlew.bat"

rm -rfv "${CURRENT_DIR}/${WORKING_DIR}/${REVIEW_SERVICE}/gradle"
rm -fv "${CURRENT_DIR}/${WORKING_DIR}/${REVIEW_SERVICE}/gradlew"
rm -fv "${CURRENT_DIR}/${WORKING_DIR}/${REVIEW_SERVICE}/gradlew.bat"

rm -rfv "${CURRENT_DIR}/${WORKING_DIR}/${PRODUCT_COMPOSITE_SERVICE}/gradle"
rm -fv "${CURRENT_DIR}/${WORKING_DIR}/${PRODUCT_COMPOSITE_SERVICE}/gradlew"
rm -fv "${CURRENT_DIR}/${WORKING_DIR}/${PRODUCT_COMPOSITE_SERVICE}/gradlew.bat"


# 4. Create the settings.gradle file
echo "Creating the settings.gradle file ........"
cat <<EOF > "${CURRENT_DIR}/settings.gradle"
include ':api'
include ':util'
include ':${WORKING_DIR}:${PRODUCT_SERVICE}' 
include ':${WORKING_DIR}:${REVIEW_SERVICE}' 
include ':${WORKING_DIR}:${RECOMMENDATION_SERVICE}' 
include ':${WORKING_DIR}:${PRODUCT_COMPOSITE_SERVICE}' 
EOF

# 5. Build and Verify the Multi Module project
##########################################################
echo "Building Multi Module Project ........"
cd "$CURRENT_DIR"

./gradlew build \
&& echo "$REVIEW_SERVICE build successfully" \
|| echo "$REVIEW_SERVICE build failed"

##########################################################
# utility functions ######################################
##########################################################


test_build() {
  ./gradlew build

  if [ $? -eq 0 ]
  then
    echo "$1 build successfully"
  else
    echo "$1 build failed"
  fi
}

test_spring_app() {
  echo "Testing $1 ..."
  ./gradlew build
  
  # use & to run the process asynchrounously
  ./gradlew bootRun -x test &
  local BOOT_RUN_PID=$!

  echo "$BOOT_RUN_PID"

  sleep 10
  if curl -s http://localhost:8080 >/dev/null
  then
    echo "App is running"
  else
    echo "Application failed to run"
  fi
  
  sleep 2
  # curl -X POST http://localhost:8080/actuator/shutdown

  # wait "$BOOT_RUN_PID"
  kill "$BOOT_RUN_PID"

  echo "Application stopped gracefully"
}