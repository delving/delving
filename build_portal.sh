#!/usr/bin/env sh

INSTALL="mvn clean install -Dmaven.test.skip=true"
PACKAGE="mvn clean package -Dmaven.test.skip=true"
BUILD_ALL=false

# Installation of jar is m2 repository
cd query; $INSTALL
cd ../cache; $INSTALL
cd ../database; $INSTALL
cd ../portal-core; $INSTALL

# Packaging of War files
cd ../portal-full; $PACKAGE
cd ../portal-lite; $PACKAGE
if [[ BUILD_ALL = true ]]; then
	#statements
	cd ../cache-servlet; $PACKAGE
	cd ../resolver; $PACKAGE
	cd ../dashboard; $PACKAGE
fi
