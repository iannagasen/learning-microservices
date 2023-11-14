#! /bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$DIR"
echo $(pwd)
cd ..
WORKING_DIR=$(pwd)/microservices

## Add @ComponentScan annotation for each microservice
find "$WORKING_DIR" -type f -name '*Application.java' -print0 \
| while IFS= read -r -d '' JAVA_APP_FILE
do 
  echo "$JAVA_APP_FILE"
  if ! grep -q '@ComponentScan("se.magnus")' "$JAVA_APP_FILE"
  then
    sed -i '/import org.springframework.boot.SpringApplication;/ a\import org.springframework.context.annotation.ComponentScan;' "$JAVA_APP_FILE"
    sed -i '/@SpringBootApplication/ i\@ComponentScan("se.magnus")' "$JAVA_APP_FILE"
    cat "$JAVA_APP_FILE"
  else
    echo "ComponentScan already added"
  fi 
  sleep 2
done