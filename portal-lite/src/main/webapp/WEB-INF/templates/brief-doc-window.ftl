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

<#-- image tab class assignation -->

<#assign tab = ""/>
<#assign showAll = ""/>
<#assign showText = ""/>
<#assign showImage = ""/>
<#assign showVideo = ""/>
<#assign showSound = ""/>
<#assign showText = ""/>

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
        <a href="index.html" title="Europeana lite"><img src="images/logo-small.png" alt="Delving Home"/></a>
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

<div id="main" class="grid_9">

    <div class="page">


        <div id="breadcrumbs">
            <div class="inner">
            <ul>
                <#if !result.matchDoc??>
                    <li class="first"><@spring.message 'MatchesFor_t' />:</li>
                    <#list breadcrumbs as crumb>
                        <#if !crumb.last>
                            <li><a href="${thisPage}?${crumb.href}">${crumb.display?html}</a>&#160;>&#160;</li>
                        <#else>
                            <li><strong>${crumb.display?html}</strong></li>
                        </#if>
                    </#list>
                <#else>
                    <li class="first">
                    <@spring.message 'ViewingRelatedItems_t' />
                    <#assign match = result.matchDoc/>
                    <a href="full-doc.html?&amp;uri=${match.id}">
                        <#if useCache="true"><img src="${cacheUrl}uri=${match.thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${match.type}" alt="${match.title}" height="25"/>
                        <#else><img src="${match.thumbnail}" alt="${match.title}" height="25"/>
                        </#if>
                    </a>
                </li>
                </#if>
            </ul>
            </div>
        </div>

        <div class="clear"></div>

        <div class="inner">

            <div id="objTypes">
                <div>
                <@spring.message 'Results_t' /> ${pagination.getStart()?c} - ${pagination.getLastViewableRecord()?c} <@spring.message 'Of_t' /> ${pagination.getNumFound()?c}
                </div>
                <@typeTabs_plain/>
                <@viewSelect/>
            </div>

            <div class="clear"></div>

            <div class="pagination">
                <@resultnav_styled/>
            </div>

            <div class="clear"></div>

            <#include "inc_result_table_brief.ftl"/>

            <div class="clear"></div>

            <div class="pagination">
                <@resultnav_styled/>
            </div>

        </div>

    </div>
</div>

<div id="sidebar" class="grid_3">

         <div id="search">
            <div class="inner">
                <@SearchForm "search_result"/>
            </div>
        </div>  

    <div id="facet-list">
        <div class="inner">
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

            <#--<@spring.message 'Results_t' /> ${pagination.getStart()?c} - ${pagination.getLastViewableRecord()?c} <@spring.message 'Of_t' /> ${pagination.getNumFound()?c}-->

            <#--<@spring.message 'Page_t' />:-->
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
                                href="?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${link.start?c}&amp;view=${view}"
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
                    href="?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${pagination.previousPage?c}&amp;view=${view}"
                    class="fg-button ui-state-default fg-button-icon-left ui-corner-all ${uiClassStatePrev}"
                    alt="<@spring.message 'AltPreviousPage_t' />"
                    style="margin: 0 8px;"
                    >
               <span class="ui-icon ui-icon-circle-arrow-w"></span><@spring.message 'Previous_t' />
            </a>
            <a
                    href="?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${pagination.nextPage?c}&amp;view=${view}"
                    class="fg-button ui-state-default fg-button-icon-right ui-corner-all ${uiClassStateNext}"
                    alt="<@spring.message 'AltNextPage_t' />"
                    >
                    <span class="ui-icon ui-icon-circle-arrow-e"></span><@spring.message 'Next_t' />
            </a>
        </div>
</#macro>
<#-- PLAIN  -->
<#macro resultnav_plain>




            <ul>
                <#--<li><@spring.message 'Results_t' /> ${pagination.getStart()?c} - ${pagination.getLastViewableRecord()?c} <@spring.message 'Of_t' /> ${pagination.getNumFound()?c}</li>
                <li><@spring.message 'Page_t' />:</li>-->
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
                            <a href="?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${link.start?c}&amp;view=${view}">
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
                            href="?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${pagination.previousPage?c}&amp;view=${view}"
                            alt="<@spring.message 'AltPreviousPage_t' />"
                            class="${uiClassStatePrev}"
                            >
                            <@spring.message 'Previous_t' />
                    </a>
                </li>
                <li>
                    <a
                            href="?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${pagination.nextPage?c}&amp;view=${view}"
                            alt="<@spring.message 'AltNextPage_t' />"
                            class="${uiClassStateNext}"
                            >
                            <@spring.message 'Next_t' />
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
        <@spring.message 'All_t' />
    </a>
    <a href="${thisPage}?query=${query}${textUrl!"&amp;qf=TYPE:TEXT"}&amp;tab=text&amp;view=${view}" class="fg-button ui-state-default ui-corner-all ${showText}">
        <@spring.message 'Texts_t' /><@print_tab_count showAll showText textCount />
    </a>
    <a href="${thisPage}?query=${query}${imageUrl!"&amp;qf=TYPE:IMAGE"}&amp;tab=image&amp;view=${view}" class="fg-button ui-state-default ui-corner-all ${showImage}">
        <@spring.message 'Images_t' /><@print_tab_count showAll showImage imageCount />
    </a>
    <a href="${thisPage}?query=${query}${videoUrl!"&amp;qf=TYPE:VIDEO"}&amp;tab=video&amp;view=${view}" class="fg-button ui-state-default ui-corner-all ${showVideo}">
        <@spring.message 'Videos_t' /><@print_tab_count showAll showVideo videoCount />
    </a>
    <a href="${thisPage}?query=${query}${audioUrl!"&amp;qf=TYPE:SOUND"}&amp;tab=sound&amp;view=${view}" class="fg-button ui-state-default ui-corner-all ${showSound}">
        <@spring.message 'Sounds_t' /><@print_tab_count showAll showSound audioCount />
    </a>

</#macro>

<#-- PLAIN  -->
<#macro typeTabs_plain>
    <ul>
        <li class="${showAll}">
            <a href="${thisPage}?query=${query}&amp;view=${view}">
                <@spring.message 'All_t' />
            </a>
        </li>
        <li class="${showText}">
            <a href="${thisPage}?query=${query}${textUrl!"&amp;qf=TYPE:TEXT"}&amp;tab=text&amp;view=${view}">
                <@spring.message 'Texts_t' /><@print_tab_count showAll showText textCount />
            </a>
        </li>
        <li class="${showImage}">
            <a href="${thisPage}?query=${query}${imageUrl!"&amp;qf=TYPE:IMAGE"}&amp;tab=image&amp;view=${view}">
                <@spring.message 'Images_t' /><@print_tab_count showAll showImage imageCount />
            </a>
        </li>
        <li class="${showVideo}">
            <a href="${thisPage}?query=${query}${videoUrl!"&amp;qf=TYPE:VIDEO"}&amp;tab=video&amp;view=${view}">
                <@spring.message 'Videos_t' /><@print_tab_count showAll showVideo videoCount />
            </a>
        </li>
        <li class="${showSound}">
            <a href="${thisPage}?query=${query}${audioUrl!"&amp;qf=TYPE:SOUND"}&amp;tab=sound&amp;view=${view}">
                <@spring.message 'Sounds_t' /><@print_tab_count showAll showSound audioCount />
            </a>
        </li>
    </ul>
</#macro>

<#macro viewSelect>
    <div id="viewselect" style="float:right">
        <#if queryStringForPresentation?exists>
        <#if view="table">
        <a href="${thisPage}?${queryStringForPresentation?html}&amp;view=table" title="<@spring.message 'AltTableView_t' />">&nbsp;<img src="images/btn-multiview-hi.gif" alt="<@spring.message 'AltTableView_t' />" /></a>
        <a href="${thisPage}?${queryStringForPresentation?html}&amp;view=list" title="<@spring.message 'AltListView_t' />" >&nbsp;<img src="images/btn-listview-lo.gif" alt="<@spring.message 'AltListView_t' />" /></a>

        <#else>
        <a href="${thisPage}?${queryStringForPresentation?html}&amp;view=table" title="<@spring.message 'AltTableView_t' />">&nbsp;<img src="images/btn-multiview-lo.gif" alt="<@spring.message 'AltTableView_t' />" hspace="5"/></a>
        <a href="${thisPage}?${queryStringForPresentation?html}&amp;view=list" title="<@spring.message 'AltListView_t' />">&nbsp;<img src="images/btn-listview-hi.gif" alt="<@spring.message 'AltListView_t' />" hspace="5"/></a>

        </#if>
        </#if>
    </div>
</#macro>
