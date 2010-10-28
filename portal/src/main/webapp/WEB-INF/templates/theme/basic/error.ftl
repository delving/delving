<#import "spring_form_macros.ftl" as spring />
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
<#include "delving-macros.ftl">

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

    <h3 style="margin: 60px 0 20px 0">Something went wrong! An email has been sent to inform our technical staff.</h3>
    <p style="margin: 0 0 240px 0">Please try another search.</p>

</section>

<#include "inc_footer.ftl"/>


