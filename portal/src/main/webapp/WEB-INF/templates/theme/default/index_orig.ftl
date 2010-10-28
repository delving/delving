<#compress>
<#assign thisPage = "index.html">
<#assign pageId = "in">
<#include "inc_header.ftl">

<div id="top-bar" class="grid_12">
    <div class="inner">
    <@userbar/>
    </div>
</div>

<div class="clear"></div>

<div id="main" class="home">


    <div class="grid_12">
        <img src="/${portalName}/${portalTheme}/images/abm-logo.jpg" id="logo-home" alt="Delving"/>
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

