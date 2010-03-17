# README for the Open Europeana Platform #

**Europeana** is an European Commission initiative to bring together digital objects from the four cultural heritage domains: Archives, Audio-Visual, Archives, Libraries and Museums. The Open Europeana Framework is an innovative multilingual high volume, high scalability search platform for Cultural Heritage metadata. The framework is developed using best-of-breed open-source software.

In order to promote collaboration and reuse of the software in the Cultural Heritage sector the software is developed in the open. You can download the source code at: [https://github.com/kiivihal/open-europeana/downloads](https://github.com/kiivihal/open-europeana/downloads)

Or using [git] version control

	git clone git://github.com/kiivihal/open-europeana.git

or fork it on [github.com][open-europeana]

The easiest way to contribute code to [open-europeana] is to fork the repository and make a pull-request for your changes to be integrated into the main repository. Git makes it very easy to keep branches and forked repositories in sync or to cherry-pick some changes.  

The [github.com][github] repository is kept in sync with the [europeanalabs] subversion repository. This repository is used by the Europeana Core development team to maintain the operational service at [europeana.eu]. If you want you can also anonymously checkout this repository.

	svn checkout http://www.europeanalabs.eu/svn/europeana/trunk

However, other then the [github] repository it is not possible to directly contribute code to this repository. 

## Open-Europeana versus EuropeanaLabs ##

Open Europeana is the place where new functionality for [europeana.eu] is developed. When new functionality is deemed ready for production it can be integrated into the operational service code-base at [europeanalabs]. The Europeana Office decides the feature set of the EuropeanaLabs code-base.

The main differences between Open-Europeana and EuropeanaLabs are listed in the table below.

<table>
    <tr> 
	    <th></th>
        <th align="left">Open-Europeana</th>
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

First read the documentation in the docs directory and on the [wiki](https://github.com/kiivihal/open-europeana/wikis). If you still have questions you can subscribe to the Open-Europeana dedicated mailing list: [http://groups.google.com/group/open-europeana][oe-mailinglist]. 

Some of the core committers to Open-Europeana also offer commercial installation, configuration, deployment, web-design and development support. 

* Beautiful Code BV. ([http://www.beautifulcode.eu](http://www.beautifulcode.eu "Beautiful Code BV."); geralddejong@gmail.com)
* Whitespace Webdevelopment (eric.meulen@gmail.com)
* Kuri Koer Software ([http://www.kurikoer.org](http://www.kurikoer.org); info@kurikoer.org)

## Main Contributors ##

* Sjoerd Siebinga (lead developer; core-development team; Open-Europeana contributor)
* Gerald de Jong (core-development team, Open-Europeana contributor)
* Eric van der Meulen (Open-Europeana contributor)
* Borys Omelayenko (core-development team)
* Nicola Aloia (core-development team)
* Cesare Concordia (core-development team)
* Jacob Lundqvist (core-development team)

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
[open-europeana]: https://github.com/kiivihal/open-europeana "open europeana github repository"
[oe-downloads]: https://github.com/kiivihal/open-europeana/downloads "open europeana downloads"
[github]: http://www.github.com "github.com" 
[europeanalabs-svn]: http://www.europeanalabs.eu/svn/europeana/trunk "europeana labs SVN repository"
[europeanalabs]: http://www.europeanalabs.eu "europeanaLabs.eu"
[git]: http://git-scm.com/ "Git"
[europeana.eu]: http://www.europeana.eu "europeana home"
[oe-mailinglist]: http://groups.google.com/group/open-europeana "open europeana mailing list"
