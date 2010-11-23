<#assign thisPage = "exception.html">
<#assign pageId = "exc"/>
<#assign view = "table"/>
<#assign query = ""/>
<#if RequestParameters.view?exists>
    <#assign view = "${RequestParameters.view}"/>
</#if>
<#if RequestParameters.query?exists>
    <#assign query = "${RequestParameters.query}"/>
</#if>
<#include "includeMarcos.ftl">

<@addHeader "Norvegiana", "",[],[]/>


    <div class="grid_12" id="branding">
        <h1 class="gigantic">
            <img src="/${portalName}/${portalTheme}/images/norvegiana.jpg" alt="Norvegiana" align="absmiddle"/>${portalDisplayName}
        </h1>
    </div>


<section role="main" class="grid_4 prefix_4">

    <h3 style="margin: 60px 0 20px 0">Something went wrong! An email has been sent to inform our technical staff.</h3>
    <p style="margin: 0 0 240px 0">Please try another search.</p>

</section>

<@addFooter/>


