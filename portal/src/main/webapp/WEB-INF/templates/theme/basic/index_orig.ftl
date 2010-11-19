<#compress>

    <#include "includeMarcos.ftl"/>

<@addHeader "Norvegiana", "",["index.js"],[]/>

<div id="home" rel="test">

    <div class="grid_12" id="branding">
        <h1 class="gigantic">
            <img src="/${portalName}/${portalTheme}/images/norvegiana.jpg" alt="Norvegiana" align="absmiddle"/>${portalDisplayName}
        </h1>
    </div>

    <div class="grid_12" id="userBar" role="navigation">
        <div class="inner">
        <@languageSelect/><@userBar/>
        </div>
    </div>

    <div class="grid_12" id="search">
    <@simpleSearch/>
        <noscript>
        <@spring.message 'NoScript_t' />
        </noscript>
    </div>

    <div id="information" class="grid_6">

        <div id="aboutTabs">
            <ul>
                <li><a href="#tab-0">Norsk</a></li>
                <li><a href="#tab-1">English</a></li>
            </ul>

            <div id="tab-0">
                <p>
                    Norge er med i EU-prosjektet EuropeanaLocal (<a href="http://www.europeanalocal.eu">www.europeanalocal.eu</a>).
                    ABM-utvikling er nasjonal koordinator, og haustar inn data fr&aring; regionale tenester og fr&aring; nasjonale
                    fellesl&oslash;ysingar som Digitalt Museum (<a href="http://www.digitaltmuseum.no">www.digitaltmuseum.no</a>),
                    Arkivportalen (<a href="http://www.arkivportalen.no">www.arkivportalen.no</a>) og Digitalt Fortalt (<a href="http://www.digitaltfortalt.no">www.digitaltfortalt.no</a>).
                    Denne fellesbasen har me gjeve arbeidsnamnet Norvegiana. Innhausta data i Norvegiana vert hausta inn i den europeiske fellesportalen Europeana. 
                    (<a href="http://www.europeana.eu">www.europeana.eu</a>).
                </p>

                <p>
                    M&aring;let med EuropeanaLocal er &aring; sikra at ogs&aring; samlingane til lokale og regionale arkiv og museum
                    skal bli ein del av tilbodet i Europeana. Innhaldet i Norvegiana er eit omfattande og spanande materiale.
                    Europeana byggjer p&aring; open kjeldekode og ABM-utvikling nyttar denne teknologien i arbeidet med &aring; hausta
                    inn data og gjera det klart for Europeana. Vi har valt &aring; opna dette materialet for s&oslash;king under namnet
                    Norvegiana. I teknologien fr&aring; Europeana ligg det funksjonar for s&oslash;k, analyse og presentasjon.
                </p>

                <p>
                    Me ser p&aring; Norvegiana som ei s&oslash;keteneste, men like mykje som eit fagleg utviklingsprosjekt. Korleis
                    fungerer materiale fr&aring; ulike sektorar og institusjonar n&aring;r det vert samla i eit felles format og ein
                    felles struktur?
                </p>
            </div>
            <div id="tab-1">
                <p>
                    Norway is partner in an EU-funded project called EuropeanaLocal (<a href="http://www.europeanalocal.eu">www.europeanalocal.eu</a>).
                    ABM-utvikling is the national coordinator and harvests data from regional services and from national services like Digitalt Museum
                    (<a href="http://www.digitaltmuseum.no">www.digitaltmuseum.no</a>), Arkivportalen (<a href="http://www.arkivportalen.no">www.arkivportalen.no</a>)
                    and Digitalt Fortalt (<a href="http://www.digitaltfortalt.no">www.digitaltfortalt.no</a>). We have given this harvested dataset the
                    workingtitle Norvegiana. Data from Norvegiana is harvested by Europeana  (<a href="http://www.europeana.eu">www.europeana.eu</a>).
                </p>

                <p>The target for EuropeanaLocal is to make local and regional content from archives and museums accessible through
                    Europeana. The content harvested into Norvegiana constitutes a very interesting and extensive mixture of information.
                    Europeana uses open source software and ABM-utvikling uses this technology to harvest data. We have also chosen to open
                    Norvegiana for public search. The techonology from Europeana has built in functionality for search, presentation and data-analyses.
                </p>

                <p>
                    Norvegiana is a search service presenting data from local and regionale archives and museums. Is is also a very interesting
                    research and development project. What can we learn when we merge data from different institutions and different domains
                    into one service?
                </p>
            </div>

        </div>



    </div>

    <#--<div id="" class="grid_6">-->

               <#--<a href="http://www.abm-utvikling.no/"  title="ABM-utvikling">-->
                    <#--<img src="/${portalName}/${portalTheme}/images/abm-logo.png" alt="ABM-utvikling" align="absmiddle"/>-->
                <#--</a>-->
                <#--<a href="http://www.europeanalocal.eu"  title="Europeana">-->
                    <#--<img src="/${portalName}/${portalTheme}/images/europeana-local.jpg" alt="Europeana" align="absmiddle"/>-->
                <#--</a>-->

                <#--<img src="/${portalName}/${portalTheme}/images/eu-flag.jpg" alt="ABM-utvikling" align="absmiddle"/>-->

                <#--<a href="http://www.delving.eu" title="Delving">-->
                    <#--<img src="/${portalName}/${portalTheme}/images/poweredbydelving.png" alt="Proudly Powered by Delving" align="absmiddle"/>-->
                <#--</a>        -->

    <#--</div>-->

</div>



<@addFooter/>

</#compress>

