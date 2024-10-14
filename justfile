#!/usr/bin/env just --justfile

# maven build without tests
build:
   mvn -DskipTests clean package

# dependencies tree for compile
dependencies:
  mvn dependency:tree -Dscope=compile > dependencies.txt

# display updates
updates:
  mvn versions:display-dependency-updates > updates.txt

create_image: build
    docker build --platform linux/amd64 -t gitp/kakao-auth-example -f ./deploy/build/Dockerfile .
save_image: create_image
    docker save gitp/kakao-auth-example | gzip > image.tar.gz
send_image: save_image
    scp -i ~/.ssh/pem/foo_key_pair.pem image.tar.gz ec2-user@3.36.60.43:/home/ec2-user/




