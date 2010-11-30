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

<div id="header">

    <div id="identity" class="grid_3">
        <h1>Delving</h1>
        <a href="/${portalName}/index.html" title="ABM"><img src="/${portalName}/${portalTheme}/images/abm-logo.jpg" alt="ABM"/></a>
    </div>

    <div class="grid_9">

        <div id="top-bar">
            <div class="inner">
                <@userbar/>
            </div>
        </div>

    </div>

</div>

<div class="clear"></div>

<div id="main" class="grid_9 page">

    <div id="query_breadcrumbs">
        <div class="inner">
        <dl>
            <dt><@spring.message '_portal.ui.navigation.matchesfor' />:</dt>
            <#if !result.matchDoc??>
                <#list breadcrumbs as crumb>
                    <#if !crumb.last>
                        <dd <#if crumb_index == 0>class="nobg"</#if>><a href="${thisPage}?${crumb.href}">${crumb.display?html}</a>&#160;>&#160;</dd>
                    <#else>
                        <dd <#if crumb_index == 0>class="nobg"</#if>><strong>${crumb.display?html}</strong></dd>
                    </#if>
                </#list>
            <#else>
                <dd  class="nobg">
                    <@spring.message 'ViewingRelated_portal.ui.messages.items' />
                    <#assign match = result.matchDoc/>
                    <a href="${match.fullDocUrl}">
                        <#if useCache="true"><img src="${cacheUrl}uri=${match.thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${match.type}" alt="${match.title}" height="25"/>
                        <#else><img src="${match.thumbnail}" alt="${match.title}" height="25"/>
                        </#if>
                    </a>
                </dd>
            </#if>
        </dl>
            </div>
    </div>

    <div class="clear"></div>

    <div class="inner">

    <div id="objTypes">
        <div>
        <@spring.message '_portal.ui.navigation.results' /> ${pagination.getStart()?c} - ${pagination.getLastViewableRecord()?c} <@spring.message '_portal.ui.navigation.of' /> ${pagination.getNumFound()?c}
        </div>
        <@typeTabs_plain/>
        <@sortResults/><@viewSelect/>
    </div>

    <div class="clearfix"></div>

    <div class="pagination">
        <@resultnav_styled/>
    </div>

    <div class="clearfix"></div>

    <#include "inc_result_table_brief.ftl"/>

    <div class="clearfix"></div>

    <div class="pagination">
        <@resultnav_styled/>
    </div>

    </div>

</div>



<div id="sidebar" class="grid_3">
     <div id="search">
        <div class="inner">
            <@SearchForm "search_result"/>
        </div>
    </div>
    <div class="inner">
    <div id="facet-list">
        <#include "inc_facets_lists.ftl"/>
    </div>
    </div>
</div>





<#include "inc_footer.ftl"/>


<#--------------------------------------------------------------------------------------------------------------------->
<#--  RESULT NAVIGATION/PAGINATION MACROS --->
<#--------------------------------------------------------------------------------------------------------------------->
<#macro print_tab_count showAll tabName countName>
    <#if showAll?matches("selected") || tabName?matches("selected")>
        (${countName})
    </#if>
</#macro>


<#-- UI STYLED  -->
<#macro resultnav_styled>
        <div class="fg-buttonset fg-buttonset-multi">

            <#--<@spring.message '_portal.ui.navigation.results' /> ${pagination.getStart()?c} - ${pagination.getLastViewableRecord()?c} <@spring.message '_portal.ui.navigation.of' /> ${pagination.getNumFound()?c}-->

            <#--<@spring.message '_portal.ui.navigation.page' />:-->
            <#list pagination.pageLinks as link>
            <#assign uiClassBorder = ""/>
            <#if link_index == 0>
                <#assign uiClassBorder = "ui-corner-left"/>
            </#if>
            <#if !link_has_next>
                <#assign uiClassBorder = "ui-corner-right"/>
            </#if>
                <#if link.linked>
                    <#assign lstart = link.start/>
                        <a
                                href="${thisPage}?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${link.start?c}&amp;view=${view}"
                                class="fg-button ui-state-default ${uiClassBorder}"
                        >
                            ${link.display?c}
                        </a>
                 <#else>
                    <a class="fg-button ui-state-default ui-state-active ${uiClassBorder}">
                        <strong>${link.display?c}</strong>
                    </a>
                </#if>
            </#list>

            <#assign uiClassStatePrev = ""/>
            <#assign uiClassStateNext = ""/>
            <#if !pagination.previous>
                <#assign uiClassStatePrev = "ui-state-disabled">
            </#if>
            <#if !pagination.next>
                <#assign uiClassStateNext = "ui-state-disabled">
            </#if>
            <a
                    href="${thisPage}?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${pagination.previousPage?c}&amp;view=${view}"
                    class="fg-button ui-state-default fg-button-icon-left ui-corner-all ${uiClassStatePrev}"
                    alt="<@spring.message '_action.alt.previous.page' />"
                    style="margin: 0 8px;"
                    >
               <span class="ui-icon ui-icon-circle-arrow-w"></span><@spring.message '_action.previous' />
            </a>
            <a
                    href="?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${pagination.nextPage?c}&amp;view=${view}"
                    class="fg-button ui-state-default fg-button-icon-right ui-corner-all ${uiClassStateNext}"
                    alt="<@spring.message '_action.alt.next.page' />"
                    >
                    <span class="ui-icon ui-icon-circle-arrow-e"></span><@spring.message '_portal.ui.navigation.next' />
            </a>
        </div>
</#macro>
<#-- PLAIN  -->
<#macro resultnav_plain>




            <ul>
                <#--<li><@spring.message '_portal.ui.navigation.results' /> ${pagination.getStart()?c} - ${pagination.getLastViewableRecord()?c} <@spring.message '_portal.ui.navigation.of' /> ${pagination.getNumFound()?c}</li>
                <li><@spring.message '_portal.ui.navigation.page' />:</li>-->
            <#list pagination.pageLinks as link>
                <#assign uiClassBorder = ""/>
                <#if link_index == 0>
                    <#assign uiClassBorder = "ui-corner-left"/>
                </#if>
                <#if !link_has_next>
                    <#assign uiClassBorder = "ui-corner-right"/>
                </#if>
                <li>
                    <#if link.linked>
                        <#assign lstart = link.start/>
                            <a href="${thisPage}?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${link.start?c}&amp;view=${view}">
                                ${link.display?c}
                            </a>
                     <#else>
                        <strong>${link.display?c}</strong>
                    </#if>
                </li>
            </#list>

            <#assign uiClassStatePrev = ""/>
            <#assign uiClassStateNext = ""/>
            <#if !pagination.previous>
                <#assign uiClassStatePrev = "disabled">
            </#if>
            <#if !pagination.next>
                <#assign uiClassStateNext = "disabled">
            </#if>
                <li>
                    <a
                            href="${thisPage}?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${pagination.previousPage?c}&amp;view=${view}"
                            alt="<@spring.message '_action.alt.previous.page' />"
                            class="${uiClassStatePrev}"
                            >
                            <@spring.message '_action.previous' />
                    </a>
                </li>
                <li>
                    <a
                            href="${thisPage}?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${pagination.nextPage?c}&amp;view=${view}"
                            alt="<@spring.message '_action.alt.next.page' />"
                            class="${uiClassStateNext}"
                            >
                            <@spring.message '_portal.ui.navigation.next' />
                    </a>
                </li>
            </ul>
</#macro>

<#--------------------------------------------------------------------------------------------------------------------->
<#--  TYPE TABS MACROS --->
<#--------------------------------------------------------------------------------------------------------------------->
<#-- UI STYLED  -->
<#macro typeTabs_styled>

    <a href="${thisPage}?query=${query}&amp;view=${view}" class="fg-button ui-state-default ui-corner-all ${showAll}">
        <@spring.message '_metadata.type.all' />
    </a>
    <a href="${thisPage}?query=${query}${textUrl!"&amp;qf=TYPE:TEXT"}&amp;tab=text&amp;view=${view}" class="fg-button ui-state-default ui-corner-all ${showText}">
        <@spring.message '_metadata.type.texts' /><@print_tab_count showAll showText textCount />
    </a>
    <a href="${thisPage}?query=${query}${imageUrl!"&amp;qf=TYPE:IMAGE"}&amp;tab=image&amp;view=${view}" class="fg-button ui-state-default ui-corner-all ${showImage}">
        <@spring.message '_metadata.type.images' /><@print_tab_count showAll showImage imageCount />
    </a>
    <a href="${thisPage}?query=${query}${videoUrl!"&amp;qf=TYPE:VIDEO"}&amp;tab=video&amp;view=${view}" class="fg-button ui-state-default ui-corner-all ${showVideo}">
        <@spring.message '_metadata.type.videos' /><@print_tab_count showAll showVideo videoCount />
    </a>
    <a href="${thisPage}?query=${query}${audioUrl!"&amp;qf=TYPE:SOUND"}&amp;tab=sound&amp;view=${view}" class="fg-button ui-state-default ui-corner-all ${showSound}">
        <@spring.message '_metadata.type.sounds' /><@print_tab_count showAll showSound audioCount />
    </a>

</#macro>

<#-- PLAIN  -->
<#macro typeTabs_plain>
    <ul>
        <li class="${showAll}">
            <a href="${thisPage}?query=${query}&amp;view=${view}">
                <@spring.message '_metadata.type.all' />
            </a>
        </li>
        <li class="${showText}">
            <a href="${thisPage}?query=${query}${textUrl!"&amp;qf=TYPE:TEXT"}&amp;tab=text&amp;view=${view}">
                <@spring.message '_metadata.type.texts' /><@print_tab_count showAll showText textCount />
            </a>
        </li>
        <li class="${showImage}">
            <a href="${thisPage}?query=${query}${imageUrl!"&amp;qf=TYPE:IMAGE"}&amp;tab=image&amp;view=${view}">
                <@spring.message '_metadata.type.images' /><@print_tab_count showAll showImage imageCount />
            </a>
        </li>
        <li class="${showVideo}">
            <a href="${thisPage}?query=${query}${videoUrl!"&amp;qf=TYPE:VIDEO"}&amp;tab=video&amp;view=${view}">
                <@spring.message '_metadata.type.videos' /><@print_tab_count showAll showVideo videoCount />
            </a>
        </li>
        <li class="${showSound}">
            <a href="${thisPage}?query=${query}${audioUrl!"&amp;qf=TYPE:SOUND"}&amp;tab=sound&amp;view=${view}">
                <@spring.message '_metadata.type.sounds' /><@print_tab_count showAll showSound audioCount />
            </a>
        </li>
    </ul>
</#macro>

<#macro viewSelect>
    <div id="viewselect" style="float:right">
        <#if queryStringForPresentation?exists>
        <#if view="table">
        <a href="${thisPage}?${queryStringForPresentation?html}&amp;view=table" title="<@spring.message '_action.alt.table.view' />">&nbsp;<img src="/${portalName}/${portalTheme}/images/btn-multiview-hi.gif" alt="<@spring.message '_action.alt.table.view' />" /></a>
        <a href="${thisPage}?${queryStringForPresentation?html}&amp;view=list" title="<@spring.message '_action.alt.list.view' />" >&nbsp;<img src="/${portalName}/${portalTheme}/images/btn-listview-lo.gif" alt="<@spring.message '_action.alt.list.view' />" /></a>

        <#else>
        <a href="${thisPage}?${queryStringForPresentation?html}&amp;view=table" title="<@spring.message '_action.alt.table.view' />">&nbsp;<img src="/${portalName}/${portalTheme}/images/btn-multiview-lo.gif" alt="<@spring.message '_action.alt.table.view' />" hspace="5"/></a>
        <a href="${thisPage}?${queryStringForPresentation?html}&amp;view=list" title="<@spring.message '_action.alt.list.view' />">&nbsp;<img src="/${portalName}/${portalTheme}/images/btn-listview-hi.gif" alt="<@spring.message '_action.alt.list.view' />" hspace="5"/></a>

        </#if>
        </#if>
    </div>
</#macro>

<#macro sortResults>
    <select id="sortOptions" name="sortBy" onchange="$('input#sortBy').val(this.value);$('form#form-sort').submit();">
        <option value="">Sorteren op:</option>
        <option value="title" ><@spring.message '_metadata.dc.title' /></option>
        <option value="creator"><@spring.message '_metadata.dc.creator' /></option>
        <option value="YEAR"><@spring.message '_metadata.dc.date' /></option>
        <#--<option value="COLLECTION"><@spring.message 'collection_t' /></option>-->
    </select>

    <form action="${thisPage}" method="GET" id="form-sort" style="display:none;">
        <input type="hidden" name="query" value="${justTheQuery}"/>
        <input type="hidden" name="start" value="${start}"/>
        <input type="hidden" name="view" value="${view}"/>
        <input type="hidden" name="sortBy" id="sortBy" value=""/>
    </form>
</#macro>
