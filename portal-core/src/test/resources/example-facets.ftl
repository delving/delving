<#assign facets = facets>

Here are the facet links:

<#list facets as facet>
    ${facet.type}
    <#list facet.links as link>
        <#if link.remove = true>remove<#else>add</#if> <a href="${link.url}">${link.value} (${link.count})</a>
    </#list>
</#list>

