<#import "spring.ftl" as spring >
<#-- GLOBAL ASSIGNS -->
<#if user??>
    <#assign user = user/>
</#if>
<#assign useCache = "true">
<#assign javascriptFiles = ""/>
<#assign cssFiles = ""/>
<#--
 * adminBlock
 *
 * Macro to generate an administrative block.
 * Only viewabe for role 'administrator' or 'god'
 -->
<#macro adminBlock>
    <#if user?? && (user.role == ('ROLE_ADMINISTRATOR') || user.role == ('ROLE_GOD'))>
    <div id="admin-block">
        <h4><@spring.message 'dms.administration.title' /></h4>

        <table class="user-options">
            <tbody>
                <tr>
                    <td><a href="/${portalName}/_.dml"><span class="ui-icon ui-icon-document"></span><@spring.message 'dms.administration.pages' /></a></td>
                </tr>
                <tr>
                    <td><a href="/${portalName}/_.img"><span class="ui-icon ui-icon-image"></span><@spring.message 'dms.administration.images' /></a></td>
                </tr>
                <tr>
                    <td><a href="/${portalName}/administration.html"><span class="ui-icon ui-icon-person"></span><@spring.message 'dms.administration.users' /></a></td>
                </tr>
            </tbody>
        </table>

    </div>
    </#if>
</#macro>

<#macro addJavascript fileList>
    <#if fileList??>
        <#if fileList?size &gt; 0>
             <#list fileList as file>
                 <#assign javascriptFiles>${javascriptFiles}${"\r"}<script type="text/javascript" src="/${portalName}/${portalTheme}/js/${file}"></script></#assign>
             </#list>
        </#if>
    </#if>
</#macro>

<#--
 * addCss
 *
 * generates the html for linked css pages
 * @param media : "screen" or "print" or "all"
 -->
<#macro addCss fileList media="all">
    <#if fileList??>
        <#if fileList?size &gt; 0>
             <#list fileList as file>
                 <link rel="stylesheet" type="text/css" href="/${portalName}/${portalTheme}/css/${file}" media="${media}"/>
             </#list>
        </#if>
    </#if>
</#macro>

<#--
 * addHeader
 *
 * generates the html header for the page
 * @param title : title of the page
 * @param bodyClass : class for the body tag (for css name-spacing for different color profiles
 * @param pageCssFiles : additional css files appended to the default
 * @param pageJsFiles : additional js files appended to the default
 -->
<#macro addHeader title="Delving" bodyClass="" pageJsFiles=[] pageCssFiles=[]>

    <!DOCTYPE html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
        <title>${title}</title>
        <script type="text/javascript">
            var msgRequired = "<@spring.message 'RequiredField_t'/>";
            var portalName = "/${portalName}";
            var baseThemePath = "/${portalName}/${portalTheme}";
        </script>
        <@addCss ["reset-text-grid.css","screen.css"], "screen"/>
        <#if pageCssFiles?size &gt; 0>
            <@addCss pageCssFiles/>
        </#if>
        ${cssFiles}
        <@addJavascript ["jquery-1.4.2.min.js", "jquery.cookie.js", "js_utilities.js"]/>
        <#if (pageJsFiles?size &gt; 0)>
            <@addJavascript pageJsFiles/>
        </#if>
        ${javascriptFiles}
    </head>
    <body class="${bodyClass}">
    <div class="container_12">
    <@adminBlock/>
    <div class="grid_12" id="userBar">
        <@userBar/>
        <#include "language_select.ftl"/>
    </div>
</#macro>
<#--
 * resultGrid
 *
 * generates a table with 4x4 rows of brief result data
 * @param seq : the actual result set
 -->
<#macro resultBriefGrid>
<#assign seq = briefDocs/>
<table summary="gallery view all search results" border="0">
    <caption>Results</caption>
    <#list seq?chunk(4) as row>
    <tr>
        <#list row as cell>
        <td valign="bottom" class="${cell.type}">
            <div class="brief-thumb-container">
                <#--<a href="${cell.fullDocUrl()}?${queryStringForPresentation}&amp;tab=${tab}&amp;start=${cell.index()?c}&amp;startPage=${pagination.start?c}&amp;view=${view}&amp;pageId=brd">-->
                <a href="${cell.fullDocUrl()}?${queryStringForPresentation}&amp;start=${cell.index()?c}&amp;startPage=${pagination.start?c}&amp;view=${view}&amp;pageId=brd">
                    <#if useCache="true">
                         <img
                                 class="thumb"
                                 id="thumb_${cell.index()?c}"
                                 align="middle"
                                 src="${cacheUrl}uri=${cell.thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${cell.type}" alt="<@spring.message 'AltMoreInfo_t' />"
                                 onload="checkSize(this.id,'brief',this.width);"
                                 onerror="showDefaultSmall(this,'${cell.type}')"
                                 height="110"
                          />
                    <#else>
                        <img
                                class="thumb"
                                id="thumb_${cell.index()?c}"
                                align="middle"
                                src="${cell.thumbnail}"
                                alt="Click for more information"
                                height="110"
                                onload="checkSize(this.id,'brief',this.width);"
                                onerror="showDefaultSmall(this,'${cell.type}')"
                         />
                    </#if>
                </a>
            </div>
            <div class="brief-content-container">
            <h6>
                <#--<a href="${cell.fullDocUrl()}?${queryStringForPresentation}&amp;tab=${tab}&amp;start=${cell.index()?c}&amp;startPage=${pagination.start?c}&amp;uri=${cell.id}&amp;view=${view}&amp;pageId=brd">-->
                <a href="${cell.fullDocUrl()}?${queryStringForPresentation}&amp;start=${cell.index()?c}&amp;startPage=${pagination.start?c}&amp;uri=${cell.id}&amp;view=${view}&amp;pageId=brd">
                    <@stringLimiter "${cell.title}" "40"/>
                </a>
            </h6>
            <ul>
                <#if cell.creator??>
                    <#if !(cell.creator = " " || cell.creator = "," || cell.creator = "Unknown,")>
                        <li><@stringLimiter "${cell.creator}" "120"/></li>
                    </#if>

                </#if>
                <#if cell.year != "">
                    <#if cell.year != "0000">
                        <li>${cell.year}</li>
                    </#if>

                </#if>
                <#if cell.provider != "">
                    <#assign pr = cell.provider />
                    <#if pr?length &gt; 80>
                        <#assign pr = cell.provider?substring(0, 80) + "..."/>
                    </#if>
                    <li title="${cell.provider}"><span class="provider">${pr}</span></li>
                </#if>
            </ul>
            </div>
        </td>
        </#list>
    </tr>
    </#list>
</table>
</#macro>

<#--
 * resultList
 *
 * generates a list of brief result data
 * @param seq : the actual result set
 -->
<#macro resultBriefList>
<#assign seq = briefDocs/>
<table cellspacing="1" cellpadding="0" width="100%" border="0" summary="search results" id="multi">
    <#list seq as cell>
    <tr>
        <td valign="top" width="50">
            <div class="brief-thumb-container-listview">
                <#--<a href="${cell.fullDocUrl()}?${queryStringForPresentation}&amp;tab=${tab}&amp;start=${cell.index()?c}&amp;startPage=${pagination.start?c}&amp;view=${view}&amp;pageId=brd">-->
                <a href="${cell.fullDocUrl()}?${queryStringForPresentation}&amp;start=${cell.index()?c}&amp;startPage=${pagination.start?c}&amp;view=${view}&amp;pageId=brd">
                    <#if useCache="true">
                        <img class="thumb"
                             id="thumb_${cell.index()}"
                             align="middle"
                             src="${cacheUrl}uri=${cell.thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${cell.type}"
                             alt="<@spring.message 'AltMoreInfo_t' />"
                             height="50"
                          />
                    <#else>
                        <img class="thumb"
                             id="thumb_${cell.index()?c}"
                             align="middle"
                             src="${cell.thumbnail}"
                             alt="Click for more information"
                             height="50"
                             onerror="showDefaultSmall(this,'${cell.type}')"
                         />
                    </#if>
                </a>
            </div>
        </td>
        <td class="${cell.type} ">
                <h6>
                    <#--<a class="fg-gray" href="${cell.fullDocUrl()}?${queryStringForPresentation}&amp;tab=${tab}&amp;start=${cell.index()?c}&amp;startPage=${pagination.start?c}&amp;view=${view}&amp;pageId=brd">-->
                    <a class="fg-gray" href="${cell.fullDocUrl()}?${queryStringForPresentation}&amp;start=${cell.index()?c}&amp;startPage=${pagination.start?c}&amp;view=${view}&amp;pageId=brd">
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
                </p>
        </td>
    </tr>
    </#list>
</table>
</#macro>

<#--
 * resultFacets
 *
 * Macro to generate lists of result facets and their links
 -->
<#macro resultBriefFacets>
    <#assign facetMap = result.facetMap />
    <#--${facetMap.getFacet("LANGUAGE")?starts_with("unknown")}-->
    <#assign columsize = 1 />
    <#assign facetsList = nextQueryFacets>
    <#list facetsList as facet>
        <#switch facet.type>
            <#case "LANGUAGE">
                <#if facet.links?size &gt; 0>
                   <#assign columsize = 2 />
                   <h4><@spring.message 'ByLanguage_t' /></h4>
                </#if>
                   <#break/>
            <#case "YEAR">
                <#if facet.links?size &gt; 0>
                    <#assign columsize = 2 />
                    <h4><@spring.message 'Bydate_t' /></h4>
               </#if>
               <#break/>
            <#case "TYPE">
                <#if facet.links?size &gt; 0>
                    <#assign columsize = 1 />
                    <h4><@spring.message 'Bytype_t' /></h4>
                </#if>
               <#break/>
            <#case "PROVIDER">
                <#if facet.links?size &gt; 0>
                    <#assign columsize = 1 />
                    <h4><@spring.message 'ByProvider_t' /></h4>
               </#if>
               <#break/>
            <#case "COUNTRY">
                <#if facet.links?size &gt; 0>
                    <#assign columsize = 1 />
                    <h4><@spring.message 'ByCountry_t' /></h4>
                </#if>
               <#break/>
        </#switch>

        <#assign facet_max = 20/>

        <#if facet.links?size &gt; 0>
        <div id="facetsContainer">
            <table summary="A list of facets to help refine your search">
                <#list facet.links?chunk(columsize) as row>
                    <tr>
                        <#list row as link>
                            <td align="left" style="padding: 2px;">
                            <#-- DO NOT ENCODE link.url. This is already done in the java code. Encoding it will break functionality !!!  -->
                                <#if !link.remove = true>
                                    <a class="add" href="?query=${query?html}${link.url?html}" title="${link.value}">
                                    <#--<input type="checkbox" value="" onclick="document.location.href='?query=${query?html}${link.url}';"/>-->
                                        <@stringLimiter "${link.value}" "25"/>(${link.count})
                                    </a>
                                <#else>
                                    <a class="remove" href="?query=${query?html}${link.url?html}" title="${link.value}">
                                        <@stringLimiter "${link.value}" "25"/>(${link.count})
                                    </a>
                                </#if>
                            </td>
                        </#list>
                    </tr>
                </#list>
            </table>
        </div>
        </#if>
    </#list>
</#macro>

<#--
 * resultPagination
 *
 * generates pagination links from brief results views
 -->
<#macro resultBriefPagination>
<#assign pagination = pagination/>
    <#list pagination.pageLinks as link>
        <#if link.linked>
            <#assign lstart = link.start/>
                <#--<a href="?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${link.start?c}&amp;view=${view}">-->
                <a href="?${queryStringForPresentation?html}&amp;start=${link.start?c}&amp;view=${view}">
                    ${link.display?c}
                </a>
         <#else>
            <a>
                <strong>${link.display?c}</strong>
            </a>
        </#if>
    </#list>
    <#if pagination.previous>
        <#--<a href="?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${pagination.previousPage?c}&amp;view=${view}" alt="<@spring.message 'AltPreviousPage_t' />">-->
        <a href="?${queryStringForPresentation?html}}&amp;start=${pagination.previousPage?c}&amp;view=${view}" alt="<@spring.message 'AltPreviousPage_t' />">
       <@spring.message 'Previous_t' />
    </a>
    </#if>
    <#if pagination.next>
        <#--<a href="?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${pagination.nextPage?c}&amp;view=${view}" alt="<@spring.message 'AltNextPage_t' />">-->
        <a href="?${queryStringForPresentation?html}&amp;start=${pagination.nextPage?c}&amp;view=${view}" alt="<@spring.message 'AltNextPage_t' />">
            <@spring.message 'Next_t' />
        </a>
    </#if>
</#macro>

<#--
 * resultPaginationList
 *
 * generates an unordered list of pagination links from brief results views
 -->
<#macro resultBriefPaginationList>
<#assign pagination = pagination/>
<ul>
    <#list pagination.pageLinks as link>
        <li>
            <#if link.linked>
                <#assign lstart = link.start/>
                <#--<a href="?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${link.start?c}&amp;view=${view}">-->
                <a href="?${queryStringForPresentation?html}&amp;start=${link.start?c}&amp;view=${view}">
                ${link.display?c}
                </a>
                <#else>
                    <strong>${link.display?c}</strong>
            </#if>
        </li>
    </#list>
    <#if pagination.previous>
    <li>
        <a
            <#--href="?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${pagination.previousPage?c}&amp;view=${view}"-->
            href="?${queryStringForPresentation?html}&amp;start=${pagination.previousPage?c}&amp;view=${view}"
            alt="<@spring.message 'AltPreviousPage_t' />"
        >
        <@spring.message 'Previous_t' />
        </a>
    </li>
    </#if>
    <#if pagination.next>
    <li>
        <a
            <#--href="?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${pagination.nextPage?c}&amp;view=${view}"-->
            href="?${queryStringForPresentation?html}&amp;start=${pagination.nextPage?c}&amp;view=${view}"
            alt="<@spring.message 'AltNextPage_t' />"
        >
        <@spring.message 'Next_t' />
        </a>
    </li>
    </#if>
</ul>
</#macro>

<#--
 * resultQueryBreadcrumbs
 *
 * Macro to generate a query result breadcrumbs
 -->
<#macro resultBriefQueryBreadcrumbs>
<#assign breadcrumbs = breadcrumbs/>
    <@spring.message 'MatchesFor_t' />:
        <#if !result.matchDoc??>
            <#list breadcrumbs as crumb>
                <#if !crumb.last>
                    <a href="?${crumb.href}">${crumb.display?html}</a>
                <#else>
                    <strong>${crumb.display?html}</strong>
                </#if>
            </#list>
        <#else>
            <@spring.message 'ViewingRelatedItems_t' />
            <#assign match = result.matchDoc/>
            <a href="${match.fullDocUrl}">
                <#if useCache="true"><img src="${cacheUrl}uri=${match.thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${match.type}" alt="${match.title}" height="25"/>
                <#else><img src="${match.thumbnail}" alt="${match.title}" height="25"/>
                </#if>
            </a>
        </#if>
</#macro>

<#--
 * resultQueryBreadcrumbsList
 *
 * Macro to generate a definition list with query result breadcrumbs
 -->
<#macro resultBriefQueryBreadcrumbsList>
<#assign breadcrumbs = breadcrumbs/>
    <dl class="breadcrumbs">
        <dt><@spring.message 'MatchesFor_t' />:</dt>
        <#if !result.matchDoc??>
            <#list breadcrumbs as crumb>
                <#if !crumb.last>
                    <dd <#if crumb_index == 0>class="first"</#if>><a href="?${crumb.href}">${crumb.display?html}</a></dd>
                <#else>
                    <dd <#if crumb_index == 0>class="first"</#if>><strong>${crumb.display?html}</strong></dd>
                </#if>
            </#list>
        <#else>
            <dd>
                <@spring.message 'ViewingRelatedItems_t' />
                <#assign match = result.matchDoc/>
                <a href="${match.fullDocUrl}">
                    <#if useCache="true">
                        <img src="${cacheUrl}uri=${match.thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${match.type}" alt="${match.title}" height="25"/>
                    <#else>
                        <img src="${match.thumbnail}" alt="${match.title}" height="25"/>
                    </#if>
                </a>
            </dd>
        </#if>
    </dl>
</#macro>

<#macro resultsBriefUserActions>
<#assign seq = briefDocs/>
    <#if seq?size &gt; 0>
        <h4><@spring.message 'Actions_t'/>:</h4>
            <#-- TODO: use a hidden form instead of hrefs to function without javascript? --> 
            <p class="linetop">
                <#if user??>
                    <a id="saveQuery" href="#" onclick="saveQuery('SavedSearch', '${queryToSave?url("utf-8")?js_string}', '${query?url("utf-8")?js_string}');"><@spring.message 'SaveThisSearch_t'/></a>
                <#else>
                    <a href="#" onclick="highLight('mustlogin'); return false" class="disabled"><@spring.message 'SaveThisSearch_t'/></a>
                </#if>
            </p>
            <div id="msg-save-search" class="msg-hide fg-pink"></div>
    </#if>
</#macro>

<#macro resultFullPagination>

    <#assign uiClassStatePrev = ""/>
    <#assign uiClassStateNext = ""/>
    <#assign urlNext = ""/>
    <#assign urlPrevious=""/>

    <#if pagination??>

        <#if !pagination.previous>
            <#assign uiClassStatePrev = "ui-state-disabled">
        <#else>
            <#assign urlPrevious = pagination.previousFullDocUrl/>
        </#if>
        <#if !pagination.next>
            <#assign uiClassStateNext = "ui-state-disabled">
        <#else>
            <#assign urlNext = pagination.nextFullDocUrl/>
        </#if>

        <a href="${urlPrevious}" class="fg-button ui-state-default fg-button-icon-left ui-corner-all ${uiClassStatePrev}" alt="<@spring.message 'AltPreviousPage_t' />">
            <span class="ui-icon ui-icon-circle-arrow-w"></span><@spring.message 'Previous_t' />
        </a>

        <a href="${urlNext}" class="fg-button ui-state-default fg-button-icon-right ui-corner-all ${uiClassStateNext}" alt="<@spring.message 'AltNextPage_t' />">
            <span class="ui-icon ui-icon-circle-arrow-e"></span><@spring.message 'Next_t' />
        </a>

        <#if pagination.returnToResults??>
            <a class="fg-button ui-state-default fg-button-icon-left ui-corner-all" href="${pagination.returnToResults?html}" alt="<@spring.message 'ReturnToResults_t' />"/>
               <span class="ui-icon ui-icon-circle-arrow-n"></span><@spring.message 'ReturnToResults_t' />
            </a>
        </#if>

    </#if>

</#macro>

<#macro resultFullList>
    <table summary="This table contains the metadata for the object being viewed">
        <caption>Object metadata</caption>
        <tbody>
            <@resultFullDataRow "dc_title"/>
            <@resultFullDataRow "dc_creator"/>
            <@resultFullDataRow "dc_description"/>
            <@resultFullDataRow "dc_type"/>
            <@resultFullDataRow "dc_subject"/>
            <@resultFullDataRow "dc_date"/>
            <@resultFullDataRow "dc_format"/>
            <@resultFullDataRow "dc_contributer"/>
            <@resultFullDataRow "dc_identifier"/>

        <#--<#list result.fullDoc.getFieldValueList() as field>-->
            <#--<tr>-->
                <#--<th scrope="row">${field.getKey()}</th>-->
                <#--<td>${field.getFirst()}</td>-->
            <#--</tr>-->
        <#--</#list>-->
        </tbody>
    </table>
</#macro>

<#macro resultFullDataRow key>
    <#assign keyVal = result.fullDoc.getFieldValue(key)/>
    <#if keyVal.isNotEmpty()>
        <tr>
            <th scope="row"><@spring.message '${keyVal.getKey()}_t' />:</th>
            <td>${keyVal.getFirst()}</td>
        </tr>
    </#if>
</#macro>

<#--
 * simpleSearch
 *
 * Macro to generate a simple search form.
 -->
<#macro simpleSearch>
    <form method="get" action="/${portalName}/brief-doc.html" accept-charset="UTF-8" id="formSimpleSearch">
        <input type="hidden" name="start" value="1" />
        <input type="hidden" name="view" value="${view}" />
        <fieldset>
            <legend>Search</legend>
            <#--<input name="query" id="query" type="text" title="Europeana Search" maxlength="100" />-->
            <input name="query" id="query" type="search" title="Search" maxlength="100" autofocus="true" />
            <button id="submitSimpleSearch" type="submit"><@spring.message 'Search_t' /></button>
            <a href="/${portalName}/advancedsearch.html" title="<@spring.message 'AdvancedSearch_t' />"><@spring.message 'AdvancedSearch_t' /></a>
        </fieldset>
    </form>
</#macro>

<#--
 * stringLimiter
 *
 * Macro which takes two parameters:
 * @param theStr : the string to be shortened
 * @param size : the desired length of the string.
 * It returns the shortened string with elipses at the end
 -->
<#macro stringLimiter theStr size>
    <#assign newStr = theStr>
    <#if newStr?length &gt; size?number>
    <#assign newStr = theStr?substring(0,size?number) + "...">
    </#if>
    ${newStr}
</#macro>

<#--
 * userBar
 *
 * Macro to generate a list of links to login / Register and user-saved items.
 -->
<#macro userBar>
    <ul>
        <#if !user??>
            <li id="mustlogin"><a href="/${portalName}/login.html" onclick="takeMeBack();"><@spring.message 'LogIn_t'/></a></li>
            <li><a href="/${portalName}/register-request.html"><@spring.message 'Register_t'/></a></li>
        </#if>

        <#if user??>
        <li>
            <@spring.message 'LoggedInAs_t' />: <strong>${user.userName?html}</strong> | <a
                href="/${portalName}/logout.html"><@spring.message 'LogOut_t' /></a>
        </li>
        <#if user.savedItems??>
        <li>
            <a href="/${portalName}/mine.html" onclick="$.cookie('ui-tabs-3', '1', { expires: 1 });">
                <@spring.message 'SavedItems_t' />
            </a>
            (<span id="savedItemsCount">${user.savedItems?size}</span>)
        </li>
        </#if>
        <#if user.savedSearches??>
        <li>
            <a href="/${portalName}/mine.html" onclick="$.cookie('ui-tabs-3', '2', { expires: 1 });">
                <@spring.message 'SavedSearches_t' />
            </a>
            (<span id="savedSearchesCount">${user.savedSearches?size}</span>)
        </li>
        </#if>
        <#if user.socialTags??>
        <li>
            <a href="/${portalName}/mine.html" onclick="$.cookie('ui-tabs-3', '3', { expires: 1 });">
                <@spring.message 'SavedTags_t' />
            </a>
            (<span id="savedTagsCount">${user.socialTags?size}</span>)
        </li>
        </#if>
        </#if>
    </ul>

</#macro>


<#macro viewSelect>
    <div id="viewselect">
        <#if queryStringForPresentation?exists>
        <#if view="table">
        <a href="?${queryStringForPresentation?html}&amp;view=table" title="<@spring.message 'AltTableView_t' />">&nbsp;<img src="/${portalName}/${portalTheme}/images/btn-multiview-hi.gif" alt="<@spring.message 'AltTableView_t' />" /></a>
        <a href="?${queryStringForPresentation?html}&amp;view=list" title="<@spring.message 'AltListView_t' />" >&nbsp;<img src="/${portalName}/${portalTheme}/images/btn-listview-lo.gif" alt="<@spring.message 'AltListView_t' />" /></a>

        <#else>
        <a href="?${queryStringForPresentation?html}&amp;view=table" title="<@spring.message 'AltTableView_t' />">&nbsp;<img src="/${portalName}/${portalTheme}/images/btn-multiview-lo.gif" alt="<@spring.message 'AltTableView_t' />" hspace="5"/></a>
        <a href="?${queryStringForPresentation?html}&amp;view=list" title="<@spring.message 'AltListView_t' />">&nbsp;<img src="/${portalName}/${portalTheme}/images/btn-listview-hi.gif" alt="<@spring.message 'AltListView_t' />" hspace="5"/></a>

        </#if>
        </#if>
    </div>
</#macro>

<#macro sortResults>
    <select id="sortOptions" name="sortBy" onchange="$('input#sortBy').val(this.value);$('form#form-sort').submit();">
        <option value="">Sorteren op:</option>
        <option value="title" ><@spring.message 'dc_title_t' /></option>
        <option value="creator"><@spring.message 'dc_creator_t' /></option>
        <option value="YEAR"><@spring.message 'dc_date_t' /></option>
        <#--<option value="COLLECTION"><@spring.message 'collection_t' /></option>-->
    </select>

    <#--<form action="" method="GET" id="form-sort" style="display:none;">-->
    <form action="" method="GET" id="form-sort" style="display:none;">
        <input type="hidden" name="query" value="${justTheQuery}"/>
        <input type="hidden" name="start" value="${start}"/>
        <input type="hidden" name="view" value="${view}"/>
        <input type="hidden" name="sortBy" id="sortBy" value=""/>
    </form>
</#macro>

