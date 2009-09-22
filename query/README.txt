# Copyright 2007 EDL FOUNDATION
#
# Licensed under the EUPL, Version 1.0 or as soon they
# will be approved by the European Commission - subsequent
# versions of the EUPL (the "Licence");
# you may not use this work except in compliance with the
# Licence.
# You may obtain a copy of the Licence at:
#
# http://ec.europa.eu/idabc/eupl
#
# Unless required by applicable law or agreed to in
# writing, software distributed under the Licence is
# distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
# express or implied.
# See the Licence for the specific language governing
# permissions and limitations under the Licence.



EuropeanaLabs Query Module Read Me file
------------------------------------------------------


The Europeana Query Module contains all the Java interfaces, Enums,
and exceptions shared amongst various EuropeanaLabs modules. This
is only used during development and building other packages. It has
no functionality of its own.

Consult the README_FIRST file in the root of this project on information on
how to build the complete EuropeanaLabs environment.

Building and using this module
------------------------------------------------------

// To compile and install the module with maven2 run the following
// command from the command-line.
> mvn clean install

The module is now available in your local maven2 repository and can
be used during the build of other EuropeanaLabs modules.
