Title:			Readme Dashboard Module  
Subtitle:		The dashboard is used to Adminstrate the Europeana Framework  
Author:			Sjoerd Siebinga  
Affiliation:	Open Europeana  
Date:			January 30, 2010  
Copyright:		2010 Open Europeana
				This work is licensed under a Creative Commons License.  
				http://creativecommons.org/licenses/by-sa/2.5/
Keywords:		

# Readme Dashboard Module #


<!--
Development start-up on IntelliJ

1. Modify your role

 - Launch portal on http://localhost:8983/portal/ and create an account for yourself
 - In IntelliJ open the Data Sources tab to your right
 - add data source
   URL: jdbc:postgresql:europeana
   user: europeana
   password: culture
 - you should see the list of tables in the Data Sources tab
 - open table users. You should see your own account
 - right click on the Data source, select 'Run JDBC console'
 - change your role by executing
  UPDATE users SET role='ROLE_GOD'
 - you may choose more modest roles as well :)

 Now you can use dashboard to add data.

2. Launch dashboard with GWT toolkit

 - Create a 'GWT launch configuration' for module 'dashboard'.
   You may want to add extra memory and explicit link to europeana.properties as VM parameters
   -Xmx256m -Deuropeana.properties=D:/europeana/europeana/europeana.properties
 - On Windows, it may fail to find swt-win32-3235.dll and gwt-ll.dll.
   You whould extract them from
     .m2\repository\com\google\gwt\gwt-dev\1.5.3\gwt-dev-1.5.3-windows-libs.zip
   to
     .m2\repository\com\google\gwt\gwt-dev\1.5.3\
  - Launch dashboard. You should see two windows of GWT web toolkit:
     console and
     broswer with a login prompt
  - Login in the GWT browser. It may take a while.

    Take a break, talk to your colleagues. Whatever it does it is the humanity that benefits.

  Now you should see the collections tab where you can upload a new one

3. Uploading test dataset

 - upload it from
   core/src/test/resources/test-files/92001_Ag_EU_TELtreasures.xml

 - index it. To index, a portal instance at http://localhost:8983/ should be running as it has solr in it

 You should eb able to come back to portal http://localhost:8983/portal, to search for
 *:*
 and see some results (in fact, see all records from the test dataset).
-->

## Software License ##

Licensed under the EUPL, Version 1.0 or as soon they
will be approved by the European Commission - subsequent
versions of the EUPL (the "Licence");
you may not use this work except in compliance with the
Licence.
You may obtain a copy of the Licence at:

http://ec.europa.eu/idabc/eupl

Unless required by applicable law or agreed to in
writing, software distributed under the Licence is
distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
express or implied.
See the Licence for the specific language governing
permissions and limitations under the Licence.