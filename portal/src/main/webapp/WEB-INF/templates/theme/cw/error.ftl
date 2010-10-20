<#import "spring.ftl" as spring />
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
<#include "inc_header.ftl">

<div id="main" class="grid_12">

    <h3>Er is een fout opgetreden! Een email is verstuurd naar de website administrator.</h3>
    <p>Probeer een andere zoekopdracht</p>
         <div id="search" style="margin-bottom: 12em;">
            <@SearchForm "search_home"/>
        </div>

</div>

<#include "inc_footer.ftl"/>

</body>
</html>

