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

<div class="grid_12" id="branding">
    <h1 class="large">${portalDisplayName}</h1>
</div>

<div class="grid_12" id="search" role="search">
    <@simpleSearch/>
</div>

<div class="clear"></div>

<div class="grid_8" id="results">

    <div id="nav_query_breadcrumbs">
        <@resultBriefQueryBreadcrumbs/>
    </div>

    <div class="clear"></div>

    <div class="grid_6 alpha" id="result_count">
        <@spring.message 'Results_t' /> ${pagination.getStart()?c} - ${pagination.getLastViewableRecord()?c} <@spring.message 'Of_t' /> ${pagination.getNumFound()?c}
    </div>

    <div class="grid_2" id="result_sort">
        <@sortResults/>
    </div>

    <div class="grid_2 omega" id="result_view_select">
        <@viewSelect/>
    </div>

    <div class="clear"></div>

    <div id="pagination">
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

</div>

<div class="grid_4" id="facets">
    <div id="facetList">
        <@resultFacets/>
    </div>

    <div id="userActions">
        <@resultsBriefUserActions/>
    </div>
</div>

<#include "inc_footer.ftl"/>

