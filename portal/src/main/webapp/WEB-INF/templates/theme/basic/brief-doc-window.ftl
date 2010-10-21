<#assign thisPage = "brief-doc.html"/>
<#assign pId="brief">
<#assign queryStringForPresentation = queryStringForPresentation/>
<#assign queryToSave = queryToSave />
<#if result??><#assign result = result/></#if>
<#assign allCount = 0 />
<#assign textCount = 0 />
<#assign imageCount = 0 />
<#assign videoCount = 0 />
<#assign audioCount = 0 />
<#assign showLanguage = 0 />
<#assign showYear = 0 />
<#assign showType = 0 />
<#assign showProvider = 0 />
<#assign showCountry = 0 />
<#assign showUserTags = 0 />
<#assign next = nextQueryFacets>
<#assign breadcrumbs = breadcrumbs/>
<#assign seq = briefDocs/>
<#assign pagination = pagination/>
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
<#-- image tab class assignation -->
<#assign tab = ""/><#assign showAll = ""/><#assign showText = ""/><#assign showImage = ""/><#assign showVideo = ""/><#assign showSound = ""/><#assign showText = ""/>
<#if RequestParameters.tab?exists>
    <#assign tab = RequestParameters.tab/>
    <#switch RequestParameters.tab>
        <#case "text"><#assign showText = "ui-state-active"/><#break/>
        <#case "image"><#assign showImage = "ui-state-active"/><#break/>
        <#case "video"><#assign showVideo = "ui-state-active"/><#break/>
        <#case "sound"><#assign showSound = "ui-state-active"/><#break/>
        <#default><#assign showAll = "ui-state-active"/><#break/>
    </#switch>
    <#else>
        <#assign showAll = "ui-state-active"/>
</#if>

<#include "inc_header.ftl">

<@userBar/>

<h1>${portalDisplayName}</h1>

<@simpleSearch/>

<div class="resultQueryBreadcrumbs">
    <@resultQueryBreadcrumbs/>
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
    <@resultPagination/>
</div>

<#if seq?size &gt; 0>
    <#if view = "table">
        <@resultGrid seq = seq/>
    <#elseif view = "flow">
        <@resultFlow seq = seq/>
    <#else>
        <@resultList seq = seq/>
    </#if>

<#else>
    <div id="no-result"><@spring.message 'NoItemsFound_t' /></div>
</#if>

<div class="pagination">
    <@resultPagination/>
</div>

<div id="facet-list">
    <#include "inc_facets_lists.ftl"/>
</div>

<#include "inc_footer.ftl"/>

