This page describes how to setup your development environment for the Delving Framework or a branch from it.
If you want to deploy the Delving Frame work on a server please consult Deployment.markdown.

# Software Requirements #

The following software must be installed on your machine to start developing the Delving Framework

* Java 1.6 or higher
* Maven2 2.1 or higher
* MongoDB 1.6 or higher
* A local smtp server (like postfix) or access to an smtp server (for sending registration emails)
* Git (version management system to get the source code)

## Contributing to Delving ##

If you want to contribute to Delving you will also need a valid [[github.com|http://github.com]] account and a public ([[ssh|http://en.wikipedia.org/wiki/Secure_Shell]]) key associated with it. See [[http://help.github.com/]] how to set this up.  You can [[fork|http://help.github.com/forking/]] your preferred Delving branch, develop your contribution and send us a pull request ([[pull request|http://help.github.com/pull-requests/]])

# Checking out the source-code #

At Delving we use the Git version management system, see for information [[here|http://git-scm.com/]].

First if you haven't done so already checkout the project at the source tab of the current github repository you are in. Copy the git url and execute the following command (I have used as example the git url of the Delving Master branch)

`git clone git@github.com:delving/delving.git`

If you only have read-access permissions, i.e. when you get a permission denied message, you can use the read-only url as follows:

`git clone git://github.com/delving/delving.git`

By default git wil create a directory by the name of the remote repository. In this case it would be delving. So go into this directory and build all the code with maven2 by executing the following commands:

`cd delving`

`./build_delving.sh`

This command will use maven2 to download all external dependencies and build all Delving modules. If this script executes without problems, you are ready to move to the next step.


## MongoDB ##

Go to the commandline and execute the following command:

`mongo`

If you see a prompt similar to the one below, then MongoDB is properly installed. If not consult the [[mongodb|http://www.mongodb.org/]] website for help.

	% mongo
	MongoDB shell version: 1.6.3
	connecting to: test
	>

When the Delving Application first makes contact with the MongoDB, it will create all the necessary databases and collections.

For convenience we will list some commonly used MongoDB commands below:

Show databases:

	> show dbs
	MetaRepo
	static

Select a database:

	> use MetaRepo
	switched to db MetaRepo

Show all collections in a database

	> show collections
	Datasets
	HarvestSteps
	Records.92001_Ag_EU_TELtreasures
	system.indexes

Find the first 10 records in a collection. In this case Datasets.

	> db.Datasets.find()

Find and pretty print all records in the collection Datasets.

	> db.Datesets.find().forEach(printjson)

Only find the first record in a collection.

> db['Records.92001_Ag_EU_TELtreasures'].findOne()

## Modules ##

The Europeana Core environment contains the following modules:

- Core Module (This module contains all the database access objecs, etc. It is required by the portal and dashboard modules).
- Services Module (This module contains the cache, resolve, and web-search API's)
- Portal Module (This module contains all the web-gui code of the Open Source Version. It requires the `core` module.)
- Sip-Core (This module contains the shared code between the services and sip-creator modules)
- Sip-Creator Module (This module contains the code to convert, normalise and enrich metadata records to ESE).

## Prepare the launch.properties file

Make a copy of the `launch.properties.template` and rename it `launch.properties`. Please do not commit this properties file to version control since it is customised for your development setup.

**Note**, if you are running everything locally you will only have to edit the email addresses and the smtp settings.

Place launch.properties somewhere. For launching, either

- Start the JVM with parameter -Dlaunch.properties=/path/to/launch.properties, or
- Set the environment variable 'LAUNCH_PROPERTIES' to /path/to/launch.properties

Here /path/to/launch.properties can be absolute or relative to the directory where the source is checked out in.

## Setting up your IDE ##

### Intellij Idea ###

New Project > import project from external model > maven > set root to source pom > import

### Eclipse ###

- install m2eclipse plugin (http://m2eclipse.codehaus.org/)
- go to File > import > general > maven projects > next > select root (dir that contains this file) > finish

if you have problems with finding the M2_REPO add it to Preferences > Java > Build Path > Classpath Variables > new
(then in the pop-up fill in name = M2_REPO; path = ~/.m2/repository)


## Initiate the Database, MetadataRepository and Index ##


In order to load data into the MetadataRepository and fill the Apache Solr index you have to run the class `./services/src/test/java/eu/delving/services/controller/TestContentLoading.java` from your IDE. Make sure your `-Dlaunch.properties` variable is available in the JVM parameters when you run this class from your IDE.

### Launch the Delving WebApplications environment ###

Each module with a web-app (services, portal) has a jetty starter class in the test directories. Use it to run the module during development.

    portal/src/test/java/eu/europeana/PortalStarter.java
    services/src/test/java/eu/delving/services/util/ServicesStarter.java

This allows for quick development cycles. Also all template, javascript and css files are live updated. You can find these instances of JettyStarter at `http://localhost:8080/portal/`,
`http://localhost:8983/services/` and `http://localhost:8983/solr/`.

### Launching the Sip-Creator ###

You can launch the Sip-Creator with with following class.

    sip-creator/src/main/java/eu/europeana/sip/gui/SipCreatorGUI.java

With the following VM parameters:

    -Dlaunch.properties=/path/to/property/file/launch.properties -Xmx512m
