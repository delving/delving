<#import "spring.ftl" as spring />
<#assign model = result/>
<#assign result = result/>
<#assign uri = result.fullDoc.id/>
<#assign view = "table"/>
<#assign thisPage = "full-doc.html"/>
<#compress>
<#if format??><#assign format = format/></#if>
<#if pagination??>
    <#assign pagination = pagination/>
    <#assign queryStringForPaging = pagination.queryStringForPaging />
</#if>
<#if result.fullDoc.dcTitle[0]?length &gt; 110>
    <#assign postTitle = result.fullDoc.dcTitle[0]?substring(0, 110)?url('utf-8') + "..."/>
<#else>
    <#assign postTitle = result.fullDoc.dcTitle[0]?url('utf-8')/>
</#if>
<#if result.fullDoc.dcCreator[0]?matches(" ")>
    <#assign postAuthor = "none"/>
<#else>
    <#assign postAuthor = result.fullDoc.dcCreator[0]/>
</#if>
<#-- Removed ?url('utf-8') from query assignment -->
<#if RequestParameters.query??><#assign query = "${RequestParameters.query}"/></#if>
<#include "inc_header.ftl">


<div id="header">

    <div id="identity" class="grid_3">
        <h1>Delving</h1>
        <a href="/${portalName}/index.html" title="Europeana lite"><img src="/${portalName}/images/logo-small.png" alt="Delving Home"/></a>
    </div>

    <div class="grid_9">

        <div id="top-bar">
            <div class="inner">
                <@userbar/>
            </div>
        </div>

    </div>

</div>


<div id="main" class="grid_9 page">

   <div id="breadcrumbs">
       <div class="inner">
            <#if pagination??>
                <ul>
                    <#if !query?starts_with("europeana_uri:")>
                        <li class="first"><@spring.message 'MatchesFor_t' />:</li>
                        <#list pagination.breadcrumbs as crumb>
                            <#if !crumb.last>
                                <li><a href="${thisPage}?${crumb.href}">${crumb.display?html}</a>&#160;>&#160;</li>
                            <#else>
                                <li><strong>${crumb.display?html}</strong></li>
                            </#if>
                        </#list>
                    <#else>
                        <li class="first">
                            <@spring.message 'ViewingRelatedItems_t' />
                            <#assign match = result.fullDoc />
                            <#--todo review this. It seems wrong to display the image of the current full-doc instead of the original related item search-->
                            <a href="full-doc.html?&amp;uri=${match.id}">
                            <#if useCache="true">
                                <img src="${cacheUrl}uri=${match.thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${match.type}" alt="${match.title}" height="25"/>
                            <#else>
                                <img src="${match.thumbnail}" alt="${match.title}" height="25"/>
                            </#if>
                            </a>
                        </li>
                    </#if>
                </ul>
            <#else>
                <ul>
                    <li>&#160;</li>
                </ul>
            </#if>
        </div>
    </div>

    <div class="clear"></div>

            <div class="inner">

    <div class="pagination fg-buttonset">



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
        <a
        href="${urlPrevious}"
        class="fg-button ui-state-default fg-button-icon-left ui-corner-all ${uiClassStatePrev}"
        alt="<@spring.message 'AltPreviousPage_t' />"
        >
       <span class="ui-icon ui-icon-circle-arrow-w"></span><@spring.message 'Previous_t' />
        </a>
        <a
            href="${urlNext}"
            class="fg-button ui-state-default fg-button-icon-right ui-corner-all ${uiClassStateNext}"
            alt="<@spring.message 'AltNextPage_t' />"
            >
            <span class="ui-icon ui-icon-circle-arrow-e"></span><@spring.message 'Next_t' />
        </a>

        <#if pagination.returnToResults??>
            <a
                    class="fg-button ui-state-default fg-button-icon-left ui-corner-all"
                    href="${pagination.returnToResults?html}"
                     alt="<@spring.message 'ReturnToResults_t' />"/>
               <span class="ui-icon ui-icon-circle-arrow-n"></span><@spring.message 'ReturnToResults_t' />
            </a>
        <#else>
        &#160;
        </#if>

    </#if>

    </div>

    <div class="clear"></div>

    <div id="item-detail">

        <#include "inc_result_table_full.ftl"/>

    </div>
    </div>
    <div class="clear"></div>

</div>

<div id="sidebar">
    <div id="search">
        <div class="inner">
            <@SearchForm "search_result"/>
        </div>
    </div>
    <div id="facet-list">
        <div class="inner">
            <#include "inc_related_content.ftl"/>
        </div>
    </div>
</div>

<#include "inc_footer.ftl"/>

<#macro show_array_values fieldName values showFieldName>
    <#list values as value>
        <#if !value?matches(" ") && !value?matches("0000")>
            <#if showFieldName>
                <p><strong>${fieldName}</strong> = ${value?html}</p>
            <#else>
                <p>${value?html}</p>
            </#if>
        </#if>
    </#list>
</#macro>

<#macro show_value fieldName value showFieldName>
    <#if showFieldName>
        <p><strong>${fieldName}</strong> = ${value}</p>
    <#else>
        <p>${value}</p>
    </#if>
</#macro>

<#macro simple_list values separator>
    <#list values?sort as value>
        <#if !value?matches(" ") && !value?matches("0000")>
            ${value}<#if value_has_next>${separator} </#if>
    <#--${value}${separator}-->
        </#if>
    </#list>
</#macro>

<#macro simple_list_dual values1 values2 separator>
    <#if isNonEmpty(values1) && isNonEmpty(values2)>
        <@simple_list values1 separator />${separator} <@simple_list values2 separator />
    <#elseif isNonEmpty(values1)>
        <@simple_list values1 separator />
    <#elseif isNonEmpty(values2)>
        <@simple_list values2 separator />
    </#if>
</#macro>

<#macro simple_list_truncated values separator trunk_length>
    <#list values?sort as value>
        <#if !value?matches(" ") && !value?matches("0000")>
            <@stringLimiter "${value}" "${trunk_length}"/><#if value_has_next>${separator} </#if>
    <#--${value}${separator}-->
        </#if>
    </#list>
</#macro>

<#function isNonEmpty values>
    <#assign nonEmptyValue = false />
    <#list  values?reverse as value>
        <#if !value?matches(" ") && !value?matches("0000")>
            <#assign nonEmptyValue = true />
            <#return nonEmptyValue />
        </#if>
    </#list>
    <#return nonEmptyValue />
</#function>
</#compress>