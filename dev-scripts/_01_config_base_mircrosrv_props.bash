#! /bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$DIR"
cd ../microservices

echo $(pwd)

props_builder() {
  cat <<EOF
server.port: $1
server.error.include-message: always

logging: 
  level: 
    root: INFO
    dev.agasen: DEBUG
    se.magnus: DEBUG
EOF
}

rm -fv ./product-composite-service/src/main/resources/application.properties
rm -fv ./product-service/src/main/resources/application.properties
rm -fv ./recommendation-service/src/main/resources/application.properties
rm -fv ./review-service/src/main/resources/application.properties

cat > ./product-service/src/main/resources/application.yaml <<< "$(props_builder 7001)"
cat > ./recommendation-service/src/main/resources/application.yaml <<< "$(props_builder 7002)"
cat > ./review-service/src/main/resources/application.yaml <<< "$(props_builder 7003)"
cat <<EOF > ./product-composite-service/src/main/resources/application.yaml
server.port: 7000
server.error.include-message: always

app:
  product-service:
    host: localhost
    port: 7001
  recommendation-service:
    host: localhost
    port: 7002
  review-service:
    host: localhost
    port: 7003

logging:
  level:
    root: INFO
    se.magnus: DEBUG
    dev.agasen: DEBUG
EOF