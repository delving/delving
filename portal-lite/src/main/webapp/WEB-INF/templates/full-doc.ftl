<#import "spring.ftl" as spring />
<#assign model = result/>
<#assign result = result/>
<#assign uri = result.fullDoc.id/>
<#assign view = "table"/>
<#assign thisPage = "full-doc.html"/>
<#compress>
<#if startPage??><#assign startPage = startPage/></#if>
<#if RequestParameters.view??> <#assign view = "${RequestParameters.view}"/></#if>
<#if format??><#assign format = format/></#if>
<#if pagination??>
    <#assign pagination = pagination/>
    <#assign queryStringForPaging = pagination.queryStringForPaging />
</#if>
<#if queryStringForPaging??>
    <#assign defaultQueryParams = "full-doc.html?"+queryStringForPaging+"&start="+pagination.docIdWindow.offset?c+"&uri="+result.fullDoc.id+"&view="+view />
<#else>
    <#assign defaultQueryParams = "full-doc.html?uri="+result.fullDoc.id />
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


<div id="sidebar" class="grid_3">
    <div id="identity">
            <h1>Europeana Lite</h1>
            <a href="index.html" title="Europeana lite"><img src="images/europeana_open_logo_small.jpg" alt="European Open Source"/></a>
    </div>

    <div id="facet-list">
        <#include "inc_related_content.ftl"/>
    </div>
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
        <#if query?exists>
        <ul>
            <li class="first"><@spring.message 'MatchesFor_t' />:</li>
            <li><strong><a href="#">${query?replace("%20"," ")?html}</a></strong></li>
        </ul>
        <#else> <ul>
            <li>&#160;</li>
        </ul>
        </#if>
    </div>

    <div class="clear"></div>

    <div class="pagination fg-buttonset">

    <#assign uiClassStatePrev = ""/>
    <#assign uiClassStateNext = ""/>
    <#assign urlNext = ""/>
    <#assign urlPrevious=""/>

    <#if pagination??>
        <#if !pagination.previous>
            <#assign uiClassStatePrev = "ui-state-disabled">
        <#else>
            <#assign urlPrevious = "full-doc.html?${queryStringForPaging?html}&amp;start=${pagination.previousInt?c}&amp;uri=${pagination.previousUri}&amp;view=${view}&amp;pageId=${pagination.pageId}&amp;tab=${pagination.tab}"/>
        </#if>
        <#if !pagination.next>
            <#assign uiClassStateNext = "ui-state-disabled">
        <#else>
            <#assign urlNext = "full-doc.html?${queryStringForPaging?html}&amp;start=${pagination.nextInt?c}&amp;uri=${pagination.nextUri}&amp;view=${view}&amp;pageId=${pagination.pageId}&amp;tab=${pagination.tab}"/>
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