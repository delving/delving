{
<#if facetMap.facetExists("MUNICIPALITY") >
"municipalities":  [
    <#list facetMap.getFacet("MUNICIPALITY") as facet>
        {
        "name" : "${facet.getName()}",
        "value" : "${facet.getName()?url('utf-8')}",
        "count" : "${facet.getCount()}"
        }<#if facet_has_next>,</#if>
    </#list>
]
</#if>
}