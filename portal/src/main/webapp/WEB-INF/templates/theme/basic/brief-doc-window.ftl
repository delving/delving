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

<#include "delving-macros.ftl">

<@addHeader "Norvegiana", "",["results.js"],[]/>

<section class="grid_3" role="complementary">
    <header id="branding">
        <h1 class="large">${portalDisplayName}</h1>
    </header>

    <div id="search" role="search">
        <@simpleSearch/>
    </div>

    <h3><@spring.message 'RefineYourSearch_t' /></h3>
    <nav id="facetList">
        <@resultBriefFacets "TYPE",  "dc_date", 2/>
    </nav>

    <nav id="userActions">
        <@resultsBriefUserActions/>
    </nav>
</section>

<section class="grid_9" id="results" role="main">

    <div id="nav_query_breadcrumbs">
        <h4><@resultBriefQueryBreadcrumbs/></h4>
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

    <nav id="pagination">
        <@resultBriefPagination/>
    </nav>

    <#if briefDocs?size &gt; 0>
        <#if view = "table">
            <@resultBriefGrid/>
        <#else>
            <@resultBriefList/>
        </#if>
    <#else>
        <div id="no-result"><@spring.message 'NoItemsFound_t' /></div>
    </#if>

    <nav class="pagination">
        <@resultBriefPagination/>
    </nav>

</section>


<#include "inc_footer.ftl"/>

