<#import "/spring.ftl" as spring />
<#assign model = result/>
<#assign result = result/>
<#assign uri = uri>
<#assign view = "table"/>
<#assign thisPage = "full-doc.html"/>
<#compress>
    <#if startPage??><#assign startPage = startPage/></#if>
    <#if RequestParameters.view??> <#assign view = "${RequestParameters.view}"/></#if>
    <#if format??><#assign format = format/></#if>
    <#if pagination??>
        <#assign pagination = pagination/>
        <#assign queryStringForPaging = pagination.queryStringForPaging />
    </#if>

    <#assign tab = ""/>
    <#if RequestParameters.tab?exists>
        <#assign tab = RequestParameters.tab/>
    </#if>

<#-- Removed ?url('utf-8') from query assignment -->
    <#if RequestParameters.query??><#assign query = "${RequestParameters.query}"/></#if>
    <#include "inc_header.ftl">

</div> <#-- this tag was opened in inc_header -->
<div id="content">
    <#include "inc_result_table_full.ftl"/>
</div>

<div id="resultnavigation">
    <#if pagination.previous>
        <a href="full-doc.html?${queryStringForPaging?html}&amp;start=${pagination.previousInt?c}&amp;uri=${pagination.previousUri}&amp;view=${view}&amp;pageId=${pagination.pageId}&amp;tab=${pagination.tab}" class="pagination">&lt;</a>
    </#if>
    &#160;&#160;
    <#if pagination.next>
        <a href="full-doc.html?${queryStringForPaging?html}&amp;start=${pagination.nextInt?c}&amp;uri=${pagination.nextUri}&amp;view=${view}&amp;pageId=${pagination.pageId}&amp;tab=${pagination.tab}" class="pagination">&gt;</a>
    </#if>
</div>

    <#include "inc_footer.ftl"/>

</#compress>