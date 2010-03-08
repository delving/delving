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
    <#include "../inc_result_table_full.ftl"/>
</div>

<div id="resultnavigation">
	<@resultnavigation/>
</div>

    <#include "../inc_footer.ftl"/>

</#compress>