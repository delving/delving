#!/usr/bin/env sh

# This is the maven install script for the Delving Framework, see http://github.com/delving/delving for more information.

INSTALL="mvn clean install -Dmaven.test.skip=true"
PACKAGE="mvn clean package -Dmaven.test.skip=true"

# Installation of jar is m2 repository
cd metameta; $INSTALL
cd ../core; $INSTALL
cd ../sip-core; $INSTALL

# Packaging of War files
cd ../services; $PACKAGE
# Packaging the Sip-Creator Java WebStart application
cd ../sip-creator; $PACKAGE
