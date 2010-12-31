<#compress>
    <#assign thisPage = "index.html"/>
    <#include "includeMarcos.ftl"/>

<@addHeader "${portalDisplayName}", "",["index.js","jcarousel/jquery.jcarousel.min.js"],["jcarousel/tango/skin.css"]/>

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

<#--</p>-->
<#--</div>-->



<#--</section>-->
<#--<section id=leftbar" class="grid_3">-->
<#--<ul id="mycarousel" class="jcarousel jcarousel-skin-tango">-->
    <#--<#list randomItems as item>-->
        <#--<li><img src="${item.thumbnail}" width="100" height="100"></li>-->
    <#--</#list>-->
<#--</ul>-->
<#--</section>-->
<section id="info" class="grid_9">

</section>

<@addFooter/>

</#compress>

