#!/usr/bin/env sh

# This is the maven install script for Delving

INSTALL="mvn clean install -Dmaven.test.skip=true"
PACKAGE="mvn clean package -Dmaven.test.skip=true"
BUILD_ALL=true

# Installation of jar is m2 repository
cd definitions; $INSTALL
cd ../core; $INSTALL

# Packaging of War files
cd ../portal-lite; $PACKAGE
if [[ BUILD_ALL ]]; then
	#statements
	echo "building extra modules"
	cd ../api; $PACKAGE
	cd ../dashboard; $PACKAGE
	cd ../sip-creator; $PACKAGE
fi
