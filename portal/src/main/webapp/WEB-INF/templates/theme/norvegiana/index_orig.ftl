<#compress>
<#assign thisPage = "index.html"/>
<#include "includeMarcos.ftl"/>

<@addHeader "${portalDisplayName}", "",["index.js","jquery.cycle.lite.min.js"],[]/>

<#--<div id="header" class="home">-->
    <#--<h1 id="name" class="grid_3">-->
        <#--${portalDisplayName}-->
    <#--</h1>-->
    <#--<div id="userBar" class="grid_9" role="navigation">-->
        <#--<#include "language_select.ftl"/><@userBar/>-->
    <#--</div>-->
                <#--<a href="/${portalName}/" alt="Home" class="grid_3">-->
                    <#--<img id="branding" src="/${portalName}/${portalTheme}/images/logo-bubble.png" alt="" align="absmiddle"/>-->
                <#--</a>-->
<#--</div>-->

<#--<section id="home" role="main" class="shuffle">-->

    <#--<div id="search" class="grid_9">-->
        <#--<@simpleSearch/>-->
            <#--<noscript>-->
            <#--<@spring.message '_portal.ui.message.noscript' />-->
            <#--</noscript>-->
        <#--<p id="text">-->
            <#---->
        <#--</p>-->
    <#--</div>-->



<#--</section>-->

<section id="info">
    
</section>

<@addFooter/>

</#compress>

