<#compress>

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
    <#assign baseQuery = result.getPagination().getPresentationQuery().getTypeQuery()/>
    
    <#assign tab='all'/>
    <#if RequestParameters.tab?exists>
        <#assign tab = RequestParameters.tab/>
    </#if>
    <#include "includeMarcos.ftl">
  
<@addHeader "${portalDisplayName}", "",["results.js"],[]/>

<script type="text/javascript">
    var msgSearchSaveSuccess = "<@spring.message '_portal.ui.message.success.search.saved'/>";
    var msgSearchSaveFail = "<@spring.message '_mine.user.notification.failure.search.saved'/>";
</script>

<#if briefDocs?size &gt; 0>

<section class="grid_3" role="complementary">

    <dl class="menu">
        <dt><@spring.message '_action.refine.your.search' /></dt>
        <dd class="container">
            <#--<@resultBriefFacets "PROVIDER",  "_metadata.europeana.provider", 1/>-->
            <@resultBriefFacets "DATAPROVIDER",  "_metadata.abm.contentProvider", 1/>
            <@resultBriefFacets "COUNTY",  "_metadata.abm.county", 1/>
            <@resultBriefFacets "MUNICIPALITY",  "_metadata.abm.municipality", 1/>
            <@resultBriefFacets "TYPE",  "_metadata.dc.type", 1/>
            <@resultBriefFacets "HASDIGITALOBJECT",  "Has digital object", 1/>
        </dd>
    </dl>
    <@resultsBriefUserActions/>
</section>


<section class="grid_9" id="results" role="main">

    <div id="nav_query_breadcrumbs">
    <@resultBriefQueryBreadcrumbs/>
    </div>

    <div class="clear"></div>

    <div id="result_overview">

        <div id="result_count" class="grid_5 alpha">
            <div class="inner">
            <@spring.message '_portal.ui.navigation.results' /> ${pagination.getStart()?c} - ${pagination.getLastViewableRecord()?c} <@spring.message '_portal.ui.navigation.of' /> ${pagination.getNumFound()?c}
            </div>
        </div>

        <div id="result_view_select">
            <div class="inner">
            <@viewSelect/>
            </div>
        </div>

        <div id="result_sort">
            <div class="inner">
            <@sortResults/>
            </div>
        </div>

    </div>

    <div class="clear"></div>

    <div class="ui-tabs" style="padding:0 0 0 1em;">
        <ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget" id="type-tabs">
            <li class="ui-state-default ui-corner-top <#if tab = 'all'>ui-state-active</#if>">
                <a href="?${baseQuery?html}&amp;start=1&amp;view=${view}&tab=all" rel="nofollow"><@spring.message '_metadata.type.all'/></a>
            </li>
            <li class="ui-state-default ui-corner-top <#if tab = 'images'>ui-state-active</#if>">
                <a href="?${baseQuery?html}&amp;start=1&amp;view=${view}&qf=TYPE:IMAGE&tab=images" rel="nofollow"><@spring.message '_metadata.type.images'/><span><@getFacetCount result "TYPE" "IMAGE"/></span></a>
            </li>
            <li class="ui-state-default ui-corner-top <#if tab = 'texts'>ui-state-active</#if>">
                <a href="?${baseQuery?html}&amp;start=1&amp;view=${view}&qf=TYPE:TEXT&tab=texts" rel="nofollow"><@spring.message '_metadata.type.texts'/><span><@getFacetCount result "TYPE" "TEXT"/></span></a>
            </li>
            <li class="ui-state-default ui-corner-top <#if tab = 'videos'>ui-state-active</#if>">
                <a href="?${baseQuery?html}&amp;start=1&amp;view=${view}&qf=TYPE:VIDEO&tab=videos" rel="nofollow"><@spring.message '_metadata.type.videos'/><span><@getFacetCount result "TYPE" "VIDEO"/></span></a>
            </li>
            <li class="ui-state-default ui-corner-top <#if tab = 'sounds'>ui-state-active</#if>">
                <a href="?${baseQuery?html}&amp;start=1&amp;view=${view}&qf=TYPE:TEXT&tab=sounds" rel="nofollow"><@spring.message '_metadata.type.sounds'/><span><@getFacetCount result "TYPE" "AUDIO"/></span></a>
            </li>
        </ul>
    </div>

    <div class="ui-widget ui-widget-content ui-corner-all">
        <nav class="pagination">
            <div class="inner1">
            <@resultBriefPaginationStyled/>
            </div>
        </nav>

        <#if view = "table">
            <@resultBriefGrid/>
        <#else>
            <@resultBriefList/>
        </#if>

        <nav class="pagination">
            <div class="inner1">
            <@resultBriefPaginationStyled/>
            </div>
        </nav>
     </div>
 </section>
<#else>
    <section role="main" class="main no-result">
    <div class="ui-tabs" style="padding:0 0 0 .5em;">
        <ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget" id="type-tabs">
            <li class="ui-state-default ui-corner-top <#if tab = 'all'>ui-state-active</#if>">
                <a href="?${baseQuery?html}&amp;start=1&amp;view=${view}&tab=all" rel="nofollow"><@spring.message '_metadata.type.all'/></a>
            </li>
            <li class="ui-state-default ui-corner-top <#if tab = 'images'>ui-state-active</#if>">
                <a href="?${baseQuery?html}&amp;start=1&amp;view=${view}&qf=TYPE:IMAGE&tab=images" rel="nofollow"><@spring.message '_metadata.type.images'/><span><@getFacetCount result "TYPE" "IMAGE"/></span></a>
            </li>
            <li class="ui-state-default ui-corner-top <#if tab = 'texts'>ui-state-active</#if>">
                <a href="?${baseQuery?html}&amp;start=1&amp;view=${view}&qf=TYPE:TEXT&tab=texts" rel="nofollow"><@spring.message '_metadata.type.texts'/><span><@getFacetCount result "TYPE" "TEXT"/></span></a>
            </li>
            <li class="ui-state-default ui-corner-top <#if tab = 'videos'>ui-state-active</#if>">
                <a href="?${baseQuery?html}&amp;start=1&amp;view=${view}&qf=TYPE:VIDEO&tab=videos" rel="nofollow"><@spring.message '_metadata.type.videos'/><span><@getFacetCount result "TYPE" "VIDEO"/></span></a>
            </li>
            <li class="ui-state-default ui-corner-top <#if tab = 'sounds'>ui-state-active</#if>">
                <a href="?${baseQuery?html}&amp;start=1&amp;view=${view}&qf=TYPE:TEXT&tab=sounds" rel="nofollow"><@spring.message '_metadata.type.sounds'/><span><@getFacetCount result "TYPE" "AUDIO"/></span></a>
            </li>
        </ul>
    </div>
        <h2 class="grid_12" style="color:#fff;margin-top: 4em;text-align:center" >
            <@spring.message '_portal.ui.notification.noitemsfound' /><br/>
            <@spring.message '_portal.ui.notification.tryAnotherSearch'/>
        </h2>


    </section>
</#if>

<@addFooter/>
</#compress>
