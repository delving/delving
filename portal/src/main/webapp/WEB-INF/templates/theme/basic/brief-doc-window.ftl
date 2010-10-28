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
        <a href="/${portalName}/" title=""/>
        <img src="/${portalName}/${portalTheme}/images/norvegiana.jpg" alt="Norvegiana"/>
        </a>
        <h1 class="large">${portalDisplayName}</h1>
    </header>

    <h3><@spring.message 'RefineYourSearch_t' /></h3>
    <nav id="facetList">
        <@resultBriefFacets "TYPE",  "dc_type", 2/>
    </nav>

    <nav id="userActions">
        <@resultsBriefUserActions/>
    </nav>
</section>

<section class="grid_9" id="results" role="main">

    <div id="userBar" role="navigation">
        <div class="inner">
        <@userBar/>
        </div>
    </div>

    <div class="clear"></div>

    <div id="search" role="search">
        <div class="inner">
            <@simpleSearch/>
        </div>
    </div>

    <div class="clear"></div>           

    <div id="nav_query_breadcrumbs">
        <div class="inner">
            <h4><@resultBriefQueryBreadcrumbs/></h4>
        </div>
    </div>

    <div class="clear"></div>

    <div class="grid_6 alpha" id="result_count">
        <div class="inner">
        <@spring.message 'Results_t' /> ${pagination.getStart()?c} - ${pagination.getLastViewableRecord()?c} <@spring.message 'Of_t' /> ${pagination.getNumFound()?c}
        </div>
    </div>

    <div class="grid_2" id="result_sort">
        <div class="inner">
        <@sortResults/>
        </div>
    </div>

    <div class="grid_2 omega" id="result_view_select">
        <div class="inner">
        <@viewSelect/>
        </div>
    </div>

    <div class="clear"></div>

    <nav class="pagination">
        <div class="inner">
            <@resultBriefPaginationStyled/>
        </div>
    </nav>

    <div class="inner">
    <#if briefDocs?size &gt; 0>
        <#if view = "table">
            <@resultBriefGrid/>
        <#else>
            <@resultBriefList/>
        </#if>
    <#else>
        <div id="no-result"><@spring.message 'NoItemsFound_t' /></div>
    </#if>
    </div>

    <nav class="pagination">
        <div class="inner">
            <@resultBriefPaginationStyled/>
        </div>
    </nav>

</section>


<#include "inc_footer.ftl"/>

