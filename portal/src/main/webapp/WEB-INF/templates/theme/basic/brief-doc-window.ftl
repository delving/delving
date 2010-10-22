<#--<#assign queryStringForPresentation = queryStringForPresentation/>-->
<#--<#assign queryToSave = queryToSave />-->
<#assign view = "table"/>
<#if RequestParameters.view??>
    <#assign view = "${RequestParameters.view}"/>
</#if>
<#if RequestParameters.start??>
    <#assign start = "${RequestParameters.start}"/>
    <#else>
        <#assign start = "1"/>
</#if>
<#if RequestParameters.query??>
    <#assign justTheQuery = "${RequestParameters.query}"/>
</#if>

<#include "inc_header.ftl">

<@addJavascript ["results.js"]/>

<@userBar/>

<h1>${portalDisplayName}</h1>

<@simpleSearch/>

<div class="resultQueryBreadcrumbs">
    <@resultBriefQueryBreadcrumbs/>
</div>

<div class="resultCount">
    <@spring.message 'Results_t' /> ${pagination.getStart()?c} - ${pagination.getLastViewableRecord()?c} <@spring.message 'Of_t' /> ${pagination.getNumFound()?c}
</div>

<div class="resultSorting">
    <@sortResults/>
</div>

<div class="resultViewSelect">
    <@viewSelect/>
</div>

<div class="pagination">
    <@resultBriefPagination/>
</div>

<#if briefDocs?size &gt; 0>
    <#if view = "table">
        <@resultBriefGrid/>
    <#else>
        <@resultBriefList/>
    </#if>
<#else>
    <div id="no-result"><@spring.message 'NoItemsFound_t' /></div>
</#if>

<div class="pagination">
    <@resultBriefPagination/>
</div>

<div id="facetList">
    <@resultFacets/>
</div>

<div id="userActions">
    <@resultsBriefUserActions/>
</div>

<#include "inc_footer.ftl"/>

