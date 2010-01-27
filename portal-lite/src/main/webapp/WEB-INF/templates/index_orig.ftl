<#compress>
<#assign thisPage = "index.html">
<#assign pageId = "in">
<#include "inc_header.ftl">

<div id="top-bar" class="grid_12">
    <@userbar/>
    <#include "language_select.ftl">
</div>

<div class="clear"></div>

<div id="main" class="home">



    <div class="grid_5"><img src="images/europeana_open_logo_large.jpg" id="logo-home" alt="Europeana Open Source"/></div>

    <div class="grid_7">
        <noscript>
            <div class="ui-widget grid_5 alpha">
                <div class="ui-state-highlight ui-corner-all" style="padding: 0pt 0.7em; margin-top: 20px;">
                    <@spring.message 'NoScript_t' />
                </div>
            </div>
        </noscript>
        <@SearchForm "search_home"/>
     </div>

</div>

<#include "inc_footer.ftl"/>
</#compress>

