<#compress>

<#--<#include "inc_header.ftl"/>-->
<#include "delving-macros.ftl"/>

<@addHeader "Norvegiana", "",["index.js"],[]/>

<div id="home">

<div class="grid_12" id="branding">
    <h1 class="gigantic"><img src="/${portalName}/${portalTheme}/images/norvegiana.jpg" alt="Norvegiana" align="absmiddle" />${portalDisplayName}</h1>
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

<div id="news" class="grid_12"></div>


</div>



<@addFooter/>

</#compress>

