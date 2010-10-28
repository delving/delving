<#compress>

<#--<#include "inc_header.ftl"/>-->
<#include "delving-macros.ftl"/>

<@addHeader "Norvegiana", "",[],[]/>

<div class="grid_12" id="branding">
    <h1 class="gigantic">${portalDisplayName}</h1>
</div>

<div class="grid_12" id="search">
    <@simpleSearch/>
    <noscript>
    <@spring.message 'NoScript_t' />
    </noscript>
</div>

<#include "inc_footer.ftl"/>

</#compress>

