<#--todo Zemyatin to add freemarker padding for the statistics page-->
<#list facetMap.availableFacets() as key>
    key: ${key}
    ${facetMap.facetExists(key)?string}
    <#list facetMap.getFacet(key) as facet>
        ${facet.getName()} + ${facet.getCount()}
    </#list>
</#list>

