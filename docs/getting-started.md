#Getting Started with Open Europeana Development

<!--
	TODO rewrite documentation to reflect changes after refactor (11-12-2009)
-->

This document shows you how to setup your local development environment to start developing on the Europeana Prototype. If you want to deploy the Europeana Prototype on a server environment, consult `deploy.md`.

## Requirements:

- Java 1.6 or higher
- Maven2 2.0.9 or higher
- Postgresql database 8.3 or higher
- ImageMagick 6.4 or higher


## Open Europeana Modules ##

The Europeana Core environment contains the following modules:

- Core Module (This module contains all the database access objecs, etc. It is required by the portal and dashboard modules).
- API Module (This module contains the cache, resolve, and web-search API's)
- Portal-lite Module (This module contains all the web-gui code of the Open Source Version. It requires the `core` module.)
- Dashboard Module (This module contains the administrative interface for the portal. Requires the `core` module).
- Sip-Creator Module (This module contains the code to convert, normalise and enrich metadata records to ESE).

Not open-sourced.
- Portal-full (This module contains all the web-gui code of the production environment. It requires `core` module).


## Setup Europeana Postgresql database ##

We assume that you have postgres installed on your system and have access to the postgres user before you execute the following commands.

create 'europeana' user

	createuser -U postgres -P -D -R europeana	

create 'europeana' database and enter password 'culture' on the password prompt

	createdb -E utf-8 -O europeana -U postgres europeana


You may see the error: "database creation failed: ERROR:  new encoding (UTF8) is incompatible with the encoding of the template database (SQL_ASCII)". In that case add the template parameter:

	createdb -E utf-8 -O europeana -U postgres europeana -T template0 #

test if you can login

	psql europeana europeana


## Build and install components in local maven2 repository ##


Build and install core module.

	cd core
	mvn clean install -Dmaven.test.skip=true


##Prepare the europeana.properties file

Make a copy of the `europeana.properties.template` and rename it `europeana.properties`. Edit it to make it reflect your system. If you have Postgresql running on the same machine, then you don't have to change the database settings. Please do not commit this properties file to version control since it is customised for your development setup.

**Note**, if you are running everything locally you will only have to edit the email addresses, imageMagick path and the smtp settings.

Place europeana.properties somewhere. For launching, either

- Start the JVM with parameter -Deuropeana.properties=/path/to/europeana.properties, or
- Set the environment variable 'EUROPEANA_PROPERTIES' to /path/to/europeana.properties

Here /path/to/europeana.properties can be absolute or relative.

## Setting up your IDE ##

### Intellij Idea ###

New Project > import project from external model > maven > set root to source pom > import

### Eclipse ###

- install m2eclipse plugin (http://m2eclipse.codehaus.org/)
- go to File > import > general > maven projects > next > select root (dir that contains this file) > finish

if you have problems with finding the M2_REPO add it to Preferences > Java > Build Path > Classpath Variables > new
(then in the pop-up fill in name = M2_REPO; path = ~/.m2/repository)


## Initiate the Database and Index ##


In order to load the static content and fill the index you have to run the class `./database/src/test/java/eu/europeana/bootstrap/LoadContent.java` from your IDE. (In eclipse make sure you run in from ${workspace_loc:europeana}, otherwise the europeana.properties file might not be found).

## Launch the Europeana environment ##

To launch the complete Europeana environment (Solr search engine, API module) you need to run the class `./core/src/test/java/eu/europeana/bootstrap/EuropeanaBackendStarter.java` from you IDE. This will launch an embedded Jetty Server at `http://localhost:8983/{api|solr}`/

Each module with a web-app (api, portal-lite) has a jetty starter class in the test directories. Use it to run the module during development. This allows for quick development cycles. Also all template, javascript and css files are live updated. You can find these instances of JettyStarter at http://localhost:8080/portal/. For these classes to have images and search capabilities you need to have an instance of EuropeanaStarter running as well.