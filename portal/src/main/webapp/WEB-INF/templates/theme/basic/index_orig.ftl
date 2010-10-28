<#compress>

<#include "inc_header.ftl">

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

