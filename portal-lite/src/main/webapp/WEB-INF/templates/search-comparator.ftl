<#--This page will contain all the code for the search Comparator-->
<#assign defaultView = defaultView >
<#assign standardView = standardView >
<#assign custom1View = custom1View >
<#assign custom2View = custom2View >
<#assign thisPage = "brief-doc.html"/>
<#assign next = defaultView.facetQueryLinks>
<#assign breadcrumbs = defaultView.pagination.breadcrumbs/>
<#assign pagination = defaultView.pagination/>
<#assign queryStringForPresentation = defaultView.pagination.presentationQuery.queryForPresentation />
<#assign tab = ""/>

<#include "inc_header.ftl">

<div id="sidebar" class="grid_3">

    <div id="identity">
        <h1>Delving</h1>
        <a href="/${portalName}/index.html" title="Delving"><img src="/${portalName}/images/logo-small.png"
                                                                 alt="Delving Home"/></a>
    </div>

    <div id="facet-list">
        <#include "inc_facets_lists.ftl"/>
    </div>


    <div id="main" class="grid_9">

    <div id="top-bar">
        <@userbar/>
        <#include "language_select.ftl">
    </div>

    <div class="clear"></div>

    <div id="search">
            <@SearchForm "search_result"/>
    </div>

    <div class="clear"></div>


    <div id="breadcrumbs">
        <ul>
            <#if !defaultView.matchDoc??>
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
                <#assign match = defaultView.matchDoc/>
                <a href="${match.fullDocUrl}">
                    <#if useCache="true"><img src="${cacheUrl}uri=${match.thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${match.type}" alt="${match.title}" height="25"/>
                    <#else><img src="${match.thumbnail}" alt="${match.title}" height="25"/>
                    </#if>
                </a>
            </li>
            </#if>
        </ul>
    </div>

    <div class="clear"></div>

    <div id="objTypes">
        <div>
        <@spring.message 'Results_t' /> ${pagination.getStart()?c} - ${pagination.getLastViewableRecord()?c} <@spring.message 'Of_t' /> ${pagination.getNumFound()?c}
        </div>
        <#--<@typeTabs_plain/>-->
        <#--<@viewSelect/>-->
    </div>

    <div class="clearfix"></div>

    <div class="pagination">
        <@resultnav_styled/>
    </div>

    <div class="clearfix"></div>

    <@show_result_list seq = defaultView.briefDocs/>

    <@show_result_list seq = standardView.briefDocs/>

    <@show_result_list seq = custom1View.briefDocs/>

    <@show_result_list seq = custom2View.briefDocs/>

        <div class="clearfix"></div>

    <div class="pagination">
        <@resultnav_styled/>
    </div>

</div>

</div>

<#include "inc_footer.ftl"/>

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

<#macro show_result_list seq>
<table cellspacing="1" cellpadding="0" width="100%" border="0" summary="search results" id="multi">
    <#list seq as cell>
    <tr>
        <td valign="top" width="50">
            <div class="brief-thumb-container-listview">
                <a href="${cell.fullDocUrl}?${queryStringForPresentation}&amp;tab=${tab}&amp;start=${cell.index?c}&amp;startPage=${pagination.start?c}&amp;view=${view}&amp;pageId=brd">
                    <#if useCache="true"><img class="thumb" id="thumb_${cell.index}" align="middle"
                                              src="${cacheUrl}uri=${cell.thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${cell.type}"
                                              alt="<@spring.message 'AltMoreInfo_t' />" height="50"/>
                    <#else><img class="thumb" id="thumb_${cell.index?c}" align="middle" src="${cell.thumbnail}"
                                alt="Click for more information" height="50"
                                onerror="showDefaultSmall(this,'${cell.type}')"/>
                    </#if>
                </a>
            </div>
        </td>
        <td class="${cell.type} ">
                <h6>
                    <a class="fg-gray" href="${cell.fullDocUrl}?${queryStringForPresentation}&amp;tab=${tab}&amp;start=${cell.index?c}&amp;startPage=${pagination.start?c}&amp;view=${view}&amp;pageId=brd">
                        <@stringLimiter "${cell.title}" "100"/></a>
                </h6>
                <p>
                <#-- without labels -->
                <#--
                <#if !cell.creator[0]?matches(" ")>${cell.creator}<br/></#if>
                <#if !cell.year?matches(" ")><#if cell.year != "0000">${cell.year}<br/></#if></#if>
                <#if !cell.provider?matches(" ")>${cell.provider}</#if>
                --->
                <#-- with labels -->
                <#if !cell.creator[0]?matches(" ")><span><@spring.message 'Creator_t' />: </span>${cell.creator}<br/></#if>
                <#if !cell.year?matches(" ")><#if cell.year != "0000"><span><@spring.message 'Date_t' />: </span>${cell.year}<br/></#if></#if>
                <#if !cell.provider?matches(" ")><@spring.message 'Provider_t' />: <span class="provider">${cell.provider}</span></#if>
                <#if cell.debugQuery??> ${cell.debugQuery}</#if>   
                </p>
        </td>
    </tr>
    </#list>
</table>
</#macro>