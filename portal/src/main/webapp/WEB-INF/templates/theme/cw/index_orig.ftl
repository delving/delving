<#compress>
<#assign thisPage = "index.html">
<#assign pageId = "in">
<#include "inc_header.ftl">



<div id="main">


    <div class="grid_12 breadcrumb">
        <em>U bevindt zich op: </em>
        <span><a href="index.html" title="Homepagina">Home</a> <span class="imgreplacement">&rsaquo;</span></span> Onderwerpen
    </div>

    <div id="search" class="grid_8">

        <h1>Vind objecten uit de Digitale Collectie Nederland. </h1>

         <noscript>
            <div class="ui-widget grid_5 alpha">
                <div class="ui-state-highlight ui-corner-all" style="padding: 0pt 0.7em; margin-top: 20px;">
                    <@spring.message 'NoScript_t' />
                </div>
            </div>
        </noscript>

        <@SearchForm "search_home"/>

    </div>

    <div class="grid_4" id="news">
        <#-- content loaded via AJAX delvingPageCall() index.js -->
    </div>

    <div class="clear"></div>

    <div class="grid_4" id="block-1">
        <#-- content loaded via AJAX delvingPageCall() index.js -->
    </div>
    <div class="grid_4" id="block-2">
        <#-- content loaded via AJAX delvingPageCall() index.js -->
    </div>
    <div class="grid_4" id="block-3">
        <#-- content loaded via AJAX delvingPageCall() index.js -->
    </div>

    <div class="clear"></div>

</div>
<#include "inc_footer.ftl"/>
</#compress>

