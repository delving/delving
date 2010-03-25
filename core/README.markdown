Title:			Readme Core Module  
Subtitle:		Subtitle  
Author:			Sjoerd Siebinga  
Affiliation:	Open Europeana  
Date:			January 30, 2010  
Copyright:		2010 Open Europeana  
				This work is licensed under a Creative Commons License.  
				http://creativecommons.org/licenses/by-sa/2.5/
Keywords:		

<!--
	TODO finish readme core module 
-->
   
# README Core Module #


The Europeana Cache Module contains the code for interacting with a
relational database which is shared amongst various EuropeanaLabs modules. This
is only used during development and building other packages. It has
no functionality of its own.

Consult the README_FIRST file in the root of this project on information on
how to build the complete EuropeanaLabs environment.


## Building and using this module

To compile and install the module with maven2 run the following command from the command-line.

	mvn clean install

The module is now available in your local maven2 repository and can
be used during the build of other EuropeanaLabs modules.



## Technologies used

This module extensively uses Hibernate (http://), Spring Framework () technologies.
In order to develop or extend this module a basic knowledge of both Hibernate and
Sping Framework (2.5 or higher) is required.

Currently, it configured to use postgresql 8.3 or higher as a back-end



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