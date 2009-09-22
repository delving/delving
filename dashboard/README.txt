Development start-up on IntelliJ

1. Modify your role

 - Launch portal on http://localhost:8983/portal/ and create an account for yourself
 - In InteeliJ open the Data Sources tab to your right
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
   -Xmx256m -Deuropeana.config=D:/europeana/europeana/europeana.properties
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
   database/src/test/resources/test-files/92001_Ag_EU_TELtreasures.xml

 - index it. To index, a portal instanse at http://localhost:8983/ should be running as it has solr in it

 You should eb able to come back to portal http://localhost:8983/portal, to search for
 *:*
 and see some results (in fact, see all records from the test dataset).
 
   
