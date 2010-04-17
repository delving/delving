<#compress>
<#assign thisPage = "index.html">
<#assign pageId = "in">
<#include "inc_header.ftl">

<div id="top-bar" class="grid_12">
    <@userbar/>
</div>

<div class="clear"></div>

<div id="main" class="home">



    <div class="grid_12">
        <img src="images/logo.png" id="logo-home" alt="ICN"/>
    </div>
    <div class="grid_6 prefix_3 suffix_3" id="text-home">
        <p>
        Het ICN werkt aan het ontsluiten van de Digitale Collectie Nederland. De collecties van een groeiend aantal Nederlandse musea, de collectie van het Instituut Collectie Nederland, maar ook thematische en regionale verzamelingen worden daartoe binnen ŽŽn omgeving bijeengebracht.
        </p>
        <p>
        Deze site is een ÔProof of ConceptÕ waarin gebruik wordt gemaakt van de open source versie van het Europeana framework.
        </p>
    </div>
    <div class="grid_12">

         <noscript>
            <div class="ui-widget grid_5 alpha">
                <div class="ui-state-highlight ui-corner-all" style="padding: 0pt 0.7em; margin-top: 20px;">
                    <@spring.message 'NoScript_t' />
                </div>
            </div>
        </noscript>
     <div id="search">
       <div class="inner">
        <@SearchForm "search_home"/>
        </div>
     </div>
    </div>





</div>

<#include "inc_footer.ftl"/>
</#compress>

