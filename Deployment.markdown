# Deployment of Delving Framework to a production environment #

This document describes the installation of the standard Delving Framework on a Debian compatible Linux Server. We assume that you have root access to this machine or at least sudo access to be able to install software via apt-get and start services via init.d.

## adding a delving user ##

Add a user. This user will be used to compile the applications and store the configuration files in its home directory.

	useradd -d /home/delving  delving

Set the password

	passwd delving

Set the correct permissions.

	chown -R delving:users /home/delving/

If not already installed install the *sudo* package

	aptitude install sudo

Edit the sudo file and add the *delving* user to the sudoers group. For example, add the line delving ALL=(ALL) ALL

	visudo

## Install the basic required software ##

Were we use apt you can use aptitude as well

### utils ###

	apt-get install screen
	apt-get install git-core
	apt-get install sun-java6-jdk
	apt-get install maven2

### Database ###

For the persistence layer we use mongodb, see http://www.mongodb.org/.

Firstly we need to add the mongodb sources to the  '/etc/apt/sources.list' and add repository that contains the last packages. The normal apt repositories contain very old version of mongob.

	vi /etc/apt/sources.list

Add the following lines to the top of the file.

	deb http://www.backports.org/debian lenny-backports main contrib non-free
	deb http://downloads.mongodb.org/distros/debian 5.0 10gen

Then retrieve the ssh-key for the mongodb repositories into the apt-get keychain.

	sudo apt-key adv --keyserver keyserver.ubuntu.com --recv 7F0CEB10

Then run run the following command to update the apt sources

	apt-get update

Then run the following command to actually install the lastest stable mongodb package.

	aptitude install mongodb-stable


### servers ###

Since most version of Debian do not contain tomcat6 you need to do some pinning in order to be able to install tomcat the normal way

Execute the following commands as root or with sudo

	echo 'APT::Default-Release "stable";' > /etc/apt/apt.conf

Add the Repository for Squeeze to get Tomcat6

	cat <<EOF>/etc/apt/sources.list.d/squeeze.list
	deb http://ftp.uk.debian.org/debian squeeze main contrib non-free
	deb-src http://ftp.uk.debian.org/debian squeeze main contrib non-free
	EOF

Pin tomcat.

	cat <<EOF>/etc/apt/preferences
	Package: *
	Pin: release o=Debian,a=stable
	Pin-Priority: 990
	Package: *
	Pin: release o=Debian,a=testing
	Pin-Priority: 500
	Package: tomcat6,tomcat6-admin,tomcat6-common,libtomcat6-java,libservlet2.5-java
	Pin: release o=Debian,a=testing
	Pin-Priority: 990
	EOF

Run update to get the latest versions of the repositories

	aptitude update

Install the tomcat6 package.

	aptitude install tomcat6

Configure tomcat as follows.

Edit the server.xml configuration file

	vim /var/lib/tomcat6/conf/server.xml

Uncomment the following line and add the URIEnconding if not present

	<Connector port="8009" protocol="AJP/1.3" URIEncoding="UTF-8" redirectPort="8443" />

Edit the default settings file

	vim /etc/default/tomcat6

Add the following  startup settings to JAVA_OPTS.

	-Xmx1280  -Dlaunch.properties=/home/delving/deploy/delving/delving_launch.properties  -Dsolr.solr.home=/home/delving/deploy/delving/solr/  -Dsolr.data.dir=/home/delving/deploy/delving/solr/data/

Don't worry about the paths for now. We will create them late.

Restart tomcat6 to make the new configuration available

	/etc/init.d/tomcat6 restart

Install apache2. Apache2 is mainly used redirect traffic via mod_ajp to tomcat.

	apt-get install apache2


Configure apache2 and edit the default configuration

	 vim /etc/apache2/sites-available/default

And replace it with the text below

	<VirtualHost *:80>
	        ServerAdmin sjoerd.siebinga@gmail.com
	        ServerName  norvegiana.delving.org
	        #serverAlias kurikoer.org

	        # Indexes + Directory Root.
	        #DirectoryIndex index.html
	        #DocumentRoot /var/www/cw/

	        <Proxy *>
	                Order deny,allow
	                Allow from all
	        </Proxy>

	        ProxyPass /portal ajp://localhost:8009/portal
	        ProxyPassReverse /portal ajp://localhost:8009/portal

	        ProxyPass /services ajp://localhost:8009/services
	        ProxyPassReverse /services ajp://localhost:8009/services

	        ProxyPass /solr ajp://localhost:8009/solr
	        ProxyPassReverse /solr ajp://localhost:8009/solr

	        DocumentRoot /var/www/
	        <Directory />
	                Options FollowSymLinks
	                AllowOverride None
	        </Directory>
	        <Directory /var/www/>
	                Options Indexes FollowSymLinks MultiViews
	                AllowOverride None
	                Order allow,deny
	                allow from all
	        </Directory>

	        ScriptAlias /cgi-bin/ /usr/lib/cgi-bin/
	        <Directory "/usr/lib/cgi-bin">
	                AllowOverride None
	                Options +ExecCGI -MultiViews +SymLinksIfOwnerMatch
	                Order allow,deny
	                Allow from all
	        </Directory>

	        ErrorLog /var/log/apache2/error.log

	        # Possible values include: debug, info, notice, warn, error, crit,
	        # alert, emerg.
	        LogLevel warn

	        CustomLog /var/log/apache2/access.log combined

	    Alias /doc/ "/usr/share/doc/"
	    <Directory "/usr/share/doc/">
	        Options Indexes MultiViews FollowSymLinks
	        AllowOverride None
	        Order deny,allow
	        Deny from all
	        Allow from 127.0.0.0/255.0.0.0 ::1/128
	    </Directory>

	</VirtualHost>

Enable proxy_ajp

	a2enmod proxy_ajp

Restart apache to make the new configuration available.

	/etc/init.d/apache2 restart

## Building and deploying the Delving Framework ##

We are building and managing the framework specific files from the homedirectory of the user we created in the first step. In our case we have called this user delving.

So firstly login as delving and create a directory for the projects in the home directory.

	mkdir projects

Go into that directory

	cd projects/

Clone the main delving repository

	git clone git@github.com:delving/delving.git


Go into that directory

	cd delving/

When you want to get the lastest version from the repository you can run the following command.

	git pull origin master

And run the build_delving.sh shell script. This script calls *maven2* under the hood to actually build the modules. The first time this script is run it will take a while, since all the maven2 dependencies will be downloaded and placed into ~/.m2

	./build_delving.sh

Create a deployment directory

	mkdir ~/deploy/delving/

Copy the configuration templates to the deployment directory

	cp -r launch.properties.template ~/deploy/delving/delving_launch.properties
	cp -r log4j_template.xml ~/deploy/delving/log4j.xml

Copy the Solr Home directory to the deploment directory.

	cp -R core/src/test/solr/single-core ~/deploy/delving/solr/home

Go to the deploy directory

	cd ~/deploy/delving/

Edit the log4j.xml file. This file is used to configure the logging for the Portal application.

	vim log4j.xml

Update the following line `<param name="File" value="/tmp/log/ClickStreamLogger.log"/>` with a reference to where the clickStreamLogs are logged. These logs are daily rotating.

	<param name="File" value="/home/delving/deploy/delving/logs/ClickStreamLogger.log"/>

Edit `delving_launch.properties` properties file and change where relevant. This file is well-documented.

	`vim delving_launch.properties`

Change permission of solr and logs directories, so tomcat6 can read and write to them

	sudo chown -R tomcat6:tomcat6 logs/ solr/

Restart tomcat to make the changes available

	/etc/init.d/tomcat6 restart

Copy the web applications to the solr webapp directory. The Solr war file only needs to be copied once.


	cp ~/projects/delving/portal/target/portal.war  /var/lib/tomcat6/webapps
	cp ~/projects/delving/services/target/services.war  /var/lib/tomcat6/webapps
	cp ~/projects/delving/core/src/test/solr/solr-1.4.1.war  /var/lib/tomcat6/webapps/solr.war


You can tail the log to see if everything has started up alright

	tail -f /var/lib/tomcat6/logs/catalina.out

You can find the applications under the following urls

	http://hostname:portname/portal/
	http://hostname:portname/solr/
	http://hostname:portname/services/

To insert a dummy super user execute the following commands

	mongo
	use users
	var user = {"email" : "delving@delving.eu", "enabled" : true, "last_login" : "Thu Jan 06 2011 16:48:20 GMT+0100 (CET)", "password" : "0ffcab9eb6df50c6d98c8c3a039c4e9f455b720b", "role" : "ROLE_GOD", "user_name" : "delving"}
	db.users.save(user)

Test if the user was persisted

	db.users.find()

If you already registered via the portal you can update your user by executing the following command in mongo

	db.users.update({user_name: 'delving'},{$set:{role:"ROLE_GOD"}})

Now you can login to the portal with email `delving@delving.eu` and password `jaslin23`