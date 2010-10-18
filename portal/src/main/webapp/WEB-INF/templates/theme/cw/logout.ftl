<#import "spring.ftl" as spring />
<#assign thisPage = "logout.html"/>
<#include "inc_header.ftl">



<div id="sidebar" class="grid_3">

    <div id="identity">
            <h1>${portalDisplayName}</h1>
            <a href="/${portalName}/index.html" title="${portalDisplayName}"><img src="/${portalName}/${portalTheme}/images/logo-small.png" alt="${portalDisplayName} Home"/></a>
    </div>

</div>

<div id="main" class="grid_9">

    <div id="top-bar">
        <@userbar/>
    </div>

    <div class="clear"></div>


    <h3>You have successfully logged out</h3>

</div>


<#include "inc_footer.ftl"/>
