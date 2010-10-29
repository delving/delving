<#import "spring_form_macros.ftl" as spring />
<#assign thisPage = "contact.html"/>
<#assign  pageId = "contact"/>
<#include "inc_header.ftl"/>
<div id="header">

    <div id="identity" class="grid_3">
        <h1>Delving</h1>
        <a href="/${portalName}/index.html" title="Delving"><img src="/${portalName}/${portalTheme}/images/logo-small.png" alt="Delving Home"/></a>
    </div>

    <div class="grid_9">

        <div id="top-bar">
            <div class="inner">
                <@userbar/>
            </div>
        </div>

    </div>

</div>
<@addFooter/>