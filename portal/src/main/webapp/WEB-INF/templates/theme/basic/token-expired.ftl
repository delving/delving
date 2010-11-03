<#import "spring.ftl" as spring />
<#assign query = ""/>
<#if RequestParameters.view??>
    <#assign view = "${RequestParameters.view}"/>
</#if>
<#if RequestParameters.query??>
    <#assign query = "${RequestParameters.query}"/>
</#if>
<#include "includeMarcos.ftl">

<@addHeader "Norvegiana", "",[],[]/>

<section class="grid_3">
    <header id="branding">
        <a href="/${portalName}/" title=""/>
        <img src="/${portalName}/${portalTheme}/images/norvegiana.jpg" alt="Norvegiana"/>
        </a>
        <h1 class="large">${portalDisplayName}</h1>
    </header>
</section>


<section role="main" class="grid_9">

        <h1>Error</h1>
        <br />
        <p>
            The link you used to complete registration is invalid or has expired.
            Please <a href="/${portalName}/login.html">register your email again</a> to finish registration.
        </p>

</section>

<@addFooter/>

