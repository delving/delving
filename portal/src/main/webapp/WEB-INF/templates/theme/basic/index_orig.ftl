<#compress>
<#--<#assign thisPage = "index.html">-->
<#--<#assign pageId = "in">-->
<#include "inc_header.ftl">

<@userBar/>

<h1>${portalDisplayName}</h1>

<noscript>
    <@spring.message 'NoScript_t' />
</noscript>

<@simpleSearch/>

<#include "inc_footer.ftl"/>
</#compress>

