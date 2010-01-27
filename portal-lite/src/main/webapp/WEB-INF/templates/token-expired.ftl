<#import "spring.ftl" as spring />
<#assign query = ""/>
<#if RequestParameters.view?exists>
    <#assign view = "${RequestParameters.view}"/>
</#if>
<#if RequestParameters.query?exists>
    <#assign query = "${RequestParameters.query}"/>
</#if>
<#include "spring_form_macros.ftl"/>
<#include "inc_header.ftl">

<div id="sidebar" class="grid_3">

    <div id="identity">
            <h1>Europeana Lite</h1>
            <a href="index.html" title="Europeana lite"><img src="images/europeana_open_logo_small.jpg" alt="European Open Source"/></a>
    </div>

</div>

<div id="main" class="grid_9">

    <div id="top-bar">
        <@userbar/>
        <#include "language_select.ftl">
    </div>

    <div class="clear"></div>

        <h1>Error</h1>
        <br />
        <p>
            The link you used to complete registration is invalid or has expired.
            Please <a href="login.html">register your email again</a> to finish registration.
        </p>

</div>

<#include "inc_footer.ftl"/>

</body>
</html>
