<#compress>

<#include "includeMarcos.ftl"/>

<@addHeader "${portalDisplayName}", "",[],[]/>

<section role="main" class="grid_12">

<h2>Statistics</h2>

<#--todo Zemyatin to add freemarker padding for the statistics page-->
<#list facetMap.availableFacets() as key>
    key: ${key}<br /> 
    ${facetMap.facetExists(key)?string}
    <#list facetMap.getFacet(key) as facet>
        ${facet.getName()} + ${facet.getCount()}<br/>
    </#list>
</#list>

</section>

<@addFooter/>

</#compress>