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

<#macro resultGrid seq>
<table summary="gallery view all search results" border="0">
    <caption>Results</caption>
    <#list seq?chunk(4) as row>
    <tr>
        <#list row as cell>
        <td valign="bottom" class="${cell.type}">
            <div class="brief-thumb-container">
                <a href="${cell.fullDocUrl()}?${queryStringForPresentation}&amp;tab=${tab}&amp;start=${cell.index()?c}&amp;startPage=${pagination.start?c}&amp;view=${view}&amp;pageId=brd">
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
                <a href="${cell.fullDocUrl()}?${queryStringForPresentation}&amp;tab=${tab}&amp;start=${cell.index()?c}&amp;startPage=${pagination.start?c}&amp;uri=${cell.id}&amp;view=${view}&amp;pageId=brd">
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
 * resultPagination
 *
 * generates pagination links from brief results views
 -->
<#macro resultPagination>
            <#list pagination.pageLinks as link>
                <#if link.linked>
                    <#assign lstart = link.start/>
                        <a
                                href="${thisPage}?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${link.start?c}&amp;view=${view}"
                        >
                            ${link.display?c}
                        </a>
                 <#else>
                    <a>
                        <strong>${link.display?c}</strong>
                    </a>
                </#if>
            </#list>
            <#if pagination.previous>
            <a
                    href="${thisPage}?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${pagination.previousPage?c}&amp;view=${view}"
                    alt="<@spring.message 'AltPreviousPage_t' />"
                    >
               <@spring.message 'Previous_t' />
            </a>
            </#if>
            <#if pagination.next>
            <a
                    href="?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${pagination.nextPage?c}&amp;view=${view}"
                    alt="<@spring.message 'AltNextPage_t' />"
                    >
                    <@spring.message 'Next_t' />
            </a>
            </#if>
</#macro>

<#--
 * resultPaginationList
 *
 * generates an unordered list of pagination links from brief results views
 -->
<#macro resultPaginationList>
<ul>
    <#list pagination.pageLinks as link>
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
    <#if pagination.previous>
    <li>
        <a
            href="${thisPage}?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${pagination.previousPage?c}&amp;view=${view}"
            alt="<@spring.message 'AltPreviousPage_t' />"
        >
        <@spring.message 'Previous_t' />
        </a>
    </li>
    </#if>
    <#if pagination.next>
    <li>
        <a
            href="${thisPage}?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${pagination.nextPage?c}&amp;view=${view}"
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
<#macro resultQueryBreadcrumbs>
    <@spring.message 'MatchesFor_t' />:
        <#if !result.matchDoc??>
            <#list breadcrumbs as crumb>
                <#if !crumb.last>
                    <a href="${thisPage}?${crumb.href}">${crumb.display?html}</a>
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
<#macro resultQueryBreadcrumbsList>
    <dl class="breadcrumbs">
        <dt><@spring.message 'MatchesFor_t' />:</dt>
        <#if !result.matchDoc??>
            <#list breadcrumbs as crumb>
                <#if !crumb.last>
                    <dd <#if crumb_index == 0>class="first"</#if>><a href="${thisPage}?${crumb.href}">${crumb.display?html}</a></dd>
                <#else>
                    <dd <#if crumb_index == 0>class="first"</#if>><strong>${crumb.display?html}</strong></dd>
                </#if>
            </#list>
        <#else>
            <dd>
                <@spring.message 'ViewingRelatedItems_t' />
                <#assign match = result.matchDoc/>
                <a href="${match.fullDocUrl}">
                    <#if useCache="true"><img src="${cacheUrl}uri=${match.thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${match.type}" alt="${match.title}" height="25"/>
                    <#else><img src="${match.thumbnail}" alt="${match.title}" height="25"/>
                    </#if>
                </a>
            </dd>
        </#if>
    </dl>
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
        <input name="query" id="query" type="text" title="Europeana Search" maxlength="100" />
        <button id="submitSimpleSearch" type="submit"><@spring.message 'Search_t' /></button>
        <br/>
        <a href="/${portalName}/advancedsearch.html" title="<@spring.message 'AdvancedSearch_t' />"><@spring.message 'AdvancedSearch_t' /></a>
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
        <a href="${thisPage}?${queryStringForPresentation?html}&amp;view=table" title="<@spring.message 'AltTableView_t' />">&nbsp;<img src="/${portalName}/${portalTheme}/images/btn-multiview-hi.gif" alt="<@spring.message 'AltTableView_t' />" /></a>
        <a href="${thisPage}?${queryStringForPresentation?html}&amp;view=list" title="<@spring.message 'AltListView_t' />" >&nbsp;<img src="/${portalName}/${portalTheme}/images/btn-listview-lo.gif" alt="<@spring.message 'AltListView_t' />" /></a>

        <#else>
        <a href="${thisPage}?${queryStringForPresentation?html}&amp;view=table" title="<@spring.message 'AltTableView_t' />">&nbsp;<img src="/${portalName}/${portalTheme}/images/btn-multiview-lo.gif" alt="<@spring.message 'AltTableView_t' />" hspace="5"/></a>
        <a href="${thisPage}?${queryStringForPresentation?html}&amp;view=list" title="<@spring.message 'AltListView_t' />">&nbsp;<img src="/${portalName}/${portalTheme}/images/btn-listview-hi.gif" alt="<@spring.message 'AltListView_t' />" hspace="5"/></a>

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

    <form action="${thisPage}" method="GET" id="form-sort" style="display:none;">
        <input type="hidden" name="query" value="${justTheQuery}"/>
        <input type="hidden" name="start" value="${start}"/>
        <input type="hidden" name="view" value="${view}"/>
        <input type="hidden" name="sortBy" id="sortBy" value=""/>
    </form>
</#macro>

