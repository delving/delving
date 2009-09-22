<#assign pagination = pagination>
<#if pagination.previous>
prev(${pagination.previousPage})
</#if>
<#list pagination.pageLinks as link>
    <#if link.linked>${link.display}(${link.start})<#else>${link.display}</#if>
</#list>
<#if pagination.next>
next(${pagination.nextPage})
</#if>