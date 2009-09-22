Europeana Development HOWTO

This file is found in the root of the Europeana Prototype. The purpose of this file is to show you how to setup
your local development environment to start developing on the Europeana Prototype. If you want to deploy the Europeana
Prototype on a server environment, consult DEPLOY.txt


Requirements:
-------------------------

    - Java 1.6 or higher
    - Maven2 2.0.9 or higher
    - Postgresql database 8.3 or higher
    - ImageMagick 6.4 or higher


Components
------------------------

The Europeana Core environment contains the following modules:
    - Query Module (This contains the java interfaces, enums, and exceptions that are shared between all the modules).
    - Cache Module (This contains the java code to cache thumbnails. It is required by the Dashboard and Cache-servlet modules).
    - Cache-Servlet Module (This module is solely used to serve cached thumbnails. It requires the Cache module).
    - Resolver-Servlet Module (This module is used to resolve the persistent europeana uri directly to the portal full-doc page).
    - Database Module (This module contains all the database access objecs, etc. It is required by the portal and dashboard modules).
    - Portal-core Module (This module contains all the java code. It requires database and query module.)
    - Portal-lite Module (This module contains all the web-gui code of the Open Source Version. It requires database, portal-core and query module.)

    Not open-sourced yet.
    - Dashboard Module (This module contains the administrative interface for the portal. Requires database module).
    - Normaliser Module (This module contains the code to convert, normalise and enrich metadata records to ESE).
    - Portal-full (This module contains all the web-gui code of the production environment. It requires database, portal-core and query module.)



Setup Europeana postgresql database
------------------------------------

// We assume that you have postgres installed on your system and have access to the postgres user before you execute
// the following commands.

// create 'europeana' user
# createuser -U postgres -P -D -R europeana

// create 'europeana' database and enter password 'culture' on the password prompt
# createdb -E utf-8 -O europeana -U postgres europeana

// You may see the error: "database creation failed: ERROR:  new encoding (UTF8) is incompatible with the encoding of the template database (SQL_ASCII)"
// In that case add the template parameter:
# createdb -E utf-8 -O europeana -U postgres europeana -T template0

// test if you can login
# psql europeana europeana



Build and install components in local maven2 repository
-------------------------------------------------------

//Build the components from the root where this file is found.

// build and install query component.
# cd query
# mvn clean install

//build and install cache component.
# cd ../cache
# mvn clean install -Dmaven.test.skip=true

//build and install database component.
# cd ../database
# mvn clean install -Dmaven.test.skip=true

//build and install portal-core component.
# cd ../portal-core
# mvn clean install -Dmaven.test.skip=true


Prepare the europeana.properties file
-------------------------------------------------------
Make a copy of the europeana.properties.template and rename it europeana.properties. Place this file in the same directory
where you find this file. Please edit it to make it reflect your system. If you have postgres running on the same machine,
then you don't have to change the database settings.

!!! If you are running everything locally you will only have to edit the email adresses, imageMagick path and the smtp settings.


Setting up your IDE
---------------------------------

Intellij Idea

New Project > import project from external model > maven > set root to source pom > import

Eclipse

- install m2eclipse plugin (http://m2eclipse.codehaus.org/)
- go to File > import > general > maven projects > next > select root (dir that contains this file) > finish

if you have problems with finding the M2_REPO add it to Preferences > Java > Build Path > Classpath Variables > new
(then in the pop-up fill in name = M2_REPO; path = ~/.m2/repository)



Initiate the Database and Index
-----------------------------------------

In order to load the static content and fill the index you have to run the class ./bootstrap/src/test/java/eu/europeana/bootstrap/LoadContent.java
from your ide. (In eclipse make sure you run in from ${workspace_loc:europeana}, otherwise the europeana.properties file might
not be found).


Launch the Europeana environment
-----------------------------------------
To launch the complete Europeana environment portal-lite, resolver, cache, solr you need to run the class
./bootstrap/src/main/java/eu/europeana/bootstrap/EuropeanaBackendStarter.java from you ide. This will launch an embedded Jetty
Server at http://localhost:8983/{resolve|cache|solr}/

Each module with an web-app (resolve, cache-servlet, portal-lite) has a jetty starter class in the test directories. Use it
to run the module during development. This allows for quick development cycles. Also all template, javascript and css files
are live updated. You can find these instances of JettyStarter at http://localhost:8080/{portal|resolve|cache}/. For these
classes to have images and search capabilities you need to have an instance of EuropeanaStarter running as well.