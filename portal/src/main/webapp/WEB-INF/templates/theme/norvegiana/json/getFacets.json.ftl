{
<#if facetMap.facetExists("MUNICIPALITY") >
"municipalities":  [
    <#list facetMap.getFacet("MUNICIPALITY") as facet>
        {
        "name" : "${facet.getName()}",
        "count" : "${facet.getCount()}"
        }<#if facet_has_next>,</#if>
    </#list>
]
</#if>
}