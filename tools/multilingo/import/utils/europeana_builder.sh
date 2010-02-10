#!/usr/bin/env sh

#
#   ------------ Europeana deployment script ------------
#
#   Created: 2010-02-01 by Jacob.Lundqvist@gmail.com
#   Copyright: 2010  Europeana Foundation
#   License:   BSD
#   Ticket 545
#   Version:
#     0.1   Initial release
#
#
#  Description
#  -----------
#  this file can be used to install all dependencies for the Europeana Framework
#  Currently, this install script only installs on Debian based linux distros
#  and Mac os X with MacPorts
#
#
#  Variables set
#  -------------
#   $PORTS_CMD
#   Platform specific
#	OS X	PORTS_CMD   full path to ports cmd

# levels above 5 are for verbose debugging, 3 should be enough for normal operation
DEBUG_LVL=9

# If set to anything but 0, all actions changing the system will be prevented
HARMLESS=1


POSTGRESQL_VERSION="83"


# linux operating system (Debian based using apt-get)

# echo 'found debian based linux operating system'

#echo 'installing ImageMagic'
##sudo apt-get install imagemagick

#echo 'installing postgresql server 8.3 or higher'
#install postgresql8.4 +server
# init db user + db
#echo 'creating europeana database and user'

#echo 'testing for java on the PATH'
# test for java


#install java 1.6


#echo 'found Darwin Based operating system (Mac os X)'

# mac operating system (using Macports)

# check if macports exist

# print to sout 'please install macports'

log() {
 if [ $1 -le $DEBUG_LVL ]; then
   echo $2
 fi
}



#==============================================================================
#
#  Check what environment we are running on
#
#  variables declared
#  ------------------
#   INST_OS         os family, OSX linux-debian linux-generic
#   INST_OS_DETAIL  more specific info   generic ubuntu
#
environ_detect() {
    INST_OS_DETAIL="generic"
    if [ "$OSTYPE" == "darwin10.0" ]; then
        log 9 "OS/X detected"
        INST_OS="OSX"
	macports_check
    elif [ "$OSTYPE" == "linux-gnu" ]; then
        log 9 "linux-gnu detected"
        if [ -a /etc/debian_version ]; then
            log 9 "debian family detected"
            INST_OS="linux-debian"
            if [ -a /etc/depmod.d/ubuntu.conf ]; then
                log 9 "ubuntu detected"
                INST_OS_DETAIL="ubuntu"
            fi
        else
            INST_OS="linux-generic"
        fi
    fi
    log 5 "System is running $INST_OS - $INST_OS_DETAIL"
}

macports_check () {
	# makes sure macports are installed
   	log 4 "macports_check()"
	PORTS_CMD=`which port`
	if [ -n "$PORTS_CMD" ]; then
    		log 6 "macports found at: $PORTS_CMD"
		return 0
	fi
	echo ">>>  maco ports not found, download from http://www.macports.org/  <<<"
	exit 1
}

#==============================================================================
#
#  Ensure ImageMagick is installed
#
#
ImageMagick_check () {
   	log 4 "ImageMagick_check()"
    	if [ "$INST_OS" = "OSX" ]; then
        	if [ -z "`port installed | grep ImageMagick | grep active`" ]; then
            		log 1 "-->> Image magick not installed, trying to install"
            		sudo port install ImageMagick
        	fi
	else
	 	echo ">>> Failed to identify OperatingSystem, aborting"
		exit 1
    	fi
}

#==============================================================================
#
#  Ensure postgresql is installed, first main function, then all the supporting ones
#
#
postgresql_check () {
	log 4 "postgresql_check()"
	if [ "$INST_OS" = "OSX" ]; then
		postgresql_check_osx
		if [ "$?" -lt "$POSTGRESQL_VERSION" ]; then
            		log 1 "-->> postgresql-server not installed, trying to install"
			sudo port install postgresql83-server # >> /tmp/europeana-install.log 2>&1
			# Check that it is properly installed
			postgresql_check_osx
			if [ "$PG_VERS" -lt "$POSTGRESQL_VERSION" ]; then
				echo ">>>  Failure to install postgresql-server, aborting  <<<"
				exit 1
			fi
			echo "Examine the above lines, if needed create initial DB according to instructions"
			echo "When your done re-run this script"
			exit 1
			#sudo mkdir -p /opt/local/var/db/postgresql83/defaultdb
			#sudo chown postgres:postgres /opt/local/var/db/postgresql83/defaultdb
			#sudo su postgres -c '/opt/local/lib/postgresql83/bin/initdb -D /opt/local/var/db/postgresql83/defaultdb'
        	fi
    	else
	 	echo ">>> Failed to identify OperatingSystem, aborting"
		exit 1
	fi
}

postgresql_check_osx () {
	# Check what version if any is installed
	PG_VERS="`port installed | grep postgres | grep server | grep active | awk '{print $1}' | sed 's/[a-z]*\([0-9]*\).*/\1/'`"
	if [ -n "$PG_VERS" ]; then
		S=$PG_VERS
	else
		S="not found"
	fi
	log 9 "osx postgres detection, compatible version: $S"
	return $PG_VERS
}

#==============================================================================
#
#  Ensure java is installed
#
#
java_check () {
   	log 4 "java_check()"
	JAVA_PTH=`which java`
	if [ -n "$JAVA_PTH" ]; then
    		log 6 "java found at: $JAVA_PTH"
		return 0
	fi
    	if [ "$INST_OS" = "OSX" ]; then
		echo ">>>  Please install Java and re-run this script  <<<"
		exit 1
	else
	 	echo ">>> Failed to identify OperatingSystem, aborting  <<<"
		exit 1
    	fi
}

#==============================================================================
#
#  other things
#
#
pre_install () {
  log 3 "pre_install()"
  ImageMagick_check
  postgresql_check
  java_check
}

#
#  "main"
#
environ_detect
pre_install


