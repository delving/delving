Title:			How to Deploy Open Europeana  
Author:			Sjoerd Siebinga  
Affiliation:	Open Europeana 
Date:			January 30, 2010  
Copyright:		2010 Open Europeana  
				This work is licensed under a Creative Commons License.  
				http://creativecommons.org/licenses/by-sa/2.5/
Keywords:		

# How to deploy Open Europeana #

In this document you can find the instructions how to deploy the *Europeana Framework* on a server environment. For development
setup please consult *develop.md*

## Requirements ##

- Java 1.6 or higher
- linux 2.5 or higher (Debian recommended)
- postgresql database 8.3 or higher
- ImageMagick 6.4 or higher
- ESP Ghostscript 8.15.2 or higher
- A servlet container (like Tomcat / Jetty 6 or higher)
- Access to a SMTP server to send emails

## Build Components ##

Maven 2 (2.10 or higher) is used to build the _Europeana Framework_ so make sure it is installed on your system and is available from the command-line. 

Go to the root of the project, i.e. where you find the `core`, `portal-lite`, etc. module folders. You can build the components in two ways: run the `build_europeana.sh` build script or perform the steps manually.

### Build Script ###


Make the file executable and execute it.

	chmod +x build_europeana.sh
	./build_europeana.sh

### Manually ###


Build `core` component and install in your local maven2 repository (~/.m2/repository)	

	cd core
	mvn clean install -Dmaven.test.skip=true

Build `api` component.

	cd ../api
	mvn clean package

Build `portal-lite` component. 

	cd ../portal-lite
	mvn clean package

Build `dashboard` component. 

	cd ../dashboard
	mvn clean package

You can find the web-applications (henceforth referred to as war-files) in the target directories of each of the module. So you can find the `portal.war` in `./portal-lite/target/portal.war`

## Deploy components in Tomcat ##

* Copy the api.war, portal.war, dashboard.war to the tomcat deploy directory (i.e. webapps dir in the
Tomcat root) on the server.
* Copy the europeana.properties.template file from `./portal/src/main/resources/europeana.properties` to the server and change
relevant properties.
* Add the europeana properties file to the tomcat startup parameters, e.g. -Deuropeana.config=/usr/local/tomcat/conf/europeana.properties

NB Also make sure that tomcat has enough heap space. We recommend `-Xmx2048m`.

## Deploy Solr ##


Make sure that Jetty is installed. (We prefer to use Jetty for solr deployment. However, there is no reason why it cannot
be deployed on Tomcat with the other components. Just remember to increase the **heap size** of the JVM if you do so.)

* Copy `./core/src/test/solr/solr.war` to the jetty deploy directory (i.e. webapps dir in the jetty root) on the server and rename to solr.war.
* Copy the Solr Home from `./portal/src/test/solr/solr/` to the server (!recursively copy all files). Any location would do. Remember that the index is stored inside the Solr Home so make sure that there is enough disk space available where you place solr home.
* add the Solr Home to the Jetty/Tomcat startup parameters, e.g. `-Dsolr.solr.home=/path/to/solr/home`


## Initiate the database ##

When you start Tomcat the database is created automatically. However, some information needs to be provided in order for
the portal and dashboard to work properly.

If you already have a development copy of the database running please make a dump and import it on the Server.

dev machine # pg_dump -E utf-8 -U your_db_name -f your_dump_file your_db_name
server      # psql -U your_db_name europeana < your_dump_file

If you do not have a development copy of the database you can use the following command to init the database.
<!--
	TODO add command to init db.
--> 

To import and index the sample data please run the following command:
