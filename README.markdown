# README for the Delving version of the Europeana Platform #

**Europeana** is an European Commission initiative to bring together digital objects from the four cultural heritage domains: Archives, Audio-Visual, Archives, Libraries and Museums. The Europeana Framework is an innovative multilingual high volume, high scalability search platform for Cultural Heritage metadata. The framework is developed using best-of-breed open-source software.

In order to promote collaboration and reuse of the software in the Cultural Heritage sector the software is developed in the open under the name "Delving".

## Support from the Cultural Heritage Sector ##

The Delving Framework is currently being adopted by a wide variety of Cultural Heritage Institutions across Europe. The development and the current feature set would not have been possible without the support and contributions by the following organisations.

* [Europeana Foundation](http://www.europeana.eu) for donating the initial code-base to the open-source community
* [ABM-utvikling](http://www.abm-utvikling.no/), Norway for supporting the development of
	* The metadata/OAI-PMH repository
	* Sip-Creator templating and metadata upload functionality
* [Instituut Collectie Nederland](http://www.icn.nl/) ('Collectiewijzer' project), Netherlands for supporting the development of
	* The Sip-Creator collection managament and thematic grouping of metadata values support
	* The Portal Content-Management component with support of image upload, dynamic editing and creation of pages, and dynamic content on the homepage
* [Austrian Institute of Technology](http://www.ait.ac.at/), Austria for donating
	* The Annotation Component for Objects, Images, Movies and Maps (full integration planned in 2011)
* [Institut für Bibliotheks- und Informationswissenschaft](http://www.ibi.hu-berlin.de/) (Humboldt-Universität zu Berlin), Germany for supporting the development of
	* The ClickStreamLogging component and analyser with a special focus on multilingual user behaviour
	* Multilingual support of the full view page.


## Open-Source ##

You can find the source code at: [https://github.com/delving/delving/](https://github.com/delving/delving/)

Using [git] version control you can checkout the source code as follows:

	git clone git://github.com/delving/delving.git

or fork it on [github.com][delving]

The easiest way to contribute code to [delving] is to fork the repository and make a pull-request for your changes to be integrated into the main repository. Git makes it very easy to keep  branches and forked repositories in sync or to cherry-pick some changes.

The [github.com][github] repository is kept in sync with the [europeanalabs] subversion repository. This repository is used by the Europeana Core development team to maintain the operational service at [europeana.eu]. If you want you can also anonymously checkout this repository.

	svn checkout http://www.europeanalabs.eu/svn/europeana/trunk

However, other then the [github] repository it is not possible to directly contribute code to this repository.

## Delving versus EuropeanaLabs ##

Delving is the place where new functionality for [europeana.eu] is developed. When new functionality is deemed ready for production it can be integrated into the operational service code-base at [europeanalabs]. The Europeana Office decides the feature set of the EuropeanaLabs code-base.

The main differences between Delving and EuropeanaLabs are listed in the table below.

<table>
    <tr>
	    <th></th>
        <th align="left">Delving</th>
		<th align="left">EuropeanaLabs</th>
    </tr>
    <tr>
		<td>New features</td>
		<td>community</td>
		<td>Europeana Office</td>
	</tr>
    <tr>
		<td>Release management</td>
		<td>community</td>
		<td>Europeana Office</td>
	</tr>
	<tr>
		<td>Access to issue tracker and wiki</td>
		<td>everyone</td>
		<td>Europeana office and related projects</td>
	</tr>
	<tr>
		<td>Contributing Code</td>
		<td>community</td>
		<td>Core Development team and related projects</td>
	</tr>
	<tr>
		<td>Mailing list</td>
		<td>open</td>
		<td>closed</td>
	</tr>
	<tr>
		<td>Installation and configuration support</td>
		<td>community and commercial</td>
		<td>none</td>
	</tr>
	<tr>
		<td>Technology Choices</td>
		<td>community</td>
		<td>Europeana Office</td>
	</tr>
</table>

## How to get help ##

First read the documentation in the docs directory and on the [wiki](https://github.com/delving/delving/wikis). If you still have questions you can subscribe to the Delving dedicated mailing list: [delving-community@googlegroups.com][delving-mailinglist].

In addition, Delving is commercially backed by Delving BV based in Rotterdam, the Netherlands, see for more information [http://www.delving.eu](http://www.delving.eu) or send an email to  info@delving.eu

## Contributors ##

### Delving Core Contributors ###

* Gerald de Jong
* Eric van der Meulen
* Sjoerd Siebinga

### Delving Contributors ###

* Manuel Gay
* Christian Sadilek

### Europeana Core Team Developers ###

* Nicola Aloia
* Cesare Concordia
* Serkan Demirel
* Jacob Lundqvist
* Borys Omelayenko

## Software License ##

Licensed under the EUPL, Version 1.0 or as soon they
will be approved by the European Commission - subsequent
versions of the EUPL (the "Licence");
you may not use this work except in compliance with the
Licence.
You may obtain a copy of the Licence at:

[http://ec.europa.eu/idabc/eupl](http://ec.europa.eu/idabc/eupl)

Unless required by applicable law or agreed to in
writing, software distributed under the Licence is
distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
express or implied.
See the Licence for the specific language governing
permissions and limitations under the Licence.

[http://ec.europa.eu/idabc/eupl]: http://ec.europa.eu/idabc/eupl "EUPL license"
[delving]: https://github.com/delving/delving "delving github repository"
[github]: http://www.github.com "github.com"
[europeanalabs-svn]: http://www.europeanalabs.eu/svn/europeana/trunk "europeana labs SVN repository"
[europeanalabs]: http://www.europeanalabs.eu "europeanaLabs.eu"
[git]: http://git-scm.com/ "Git"
[europeana.eu]: http://www.europeana.eu "europeana home"
[delving-mailinglist]: http://groups.google.com/group/delving-community "delving mailing list"
