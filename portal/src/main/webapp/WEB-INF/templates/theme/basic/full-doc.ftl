<#assign model = result/>
<#assign result = result/>
<#assign uri = result.fullDoc.id/>

<#--<#assign thisPage = "full-doc.html"/>-->
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
<#include "delving-macros.ftl">

<@addHeader "Norvegiana", "",["results.js","fancybox/jquery.fancybox-1.3.1.pack.js"],["fancybox/jquery.fancybox-1.3.1.css"]/>

<section id="sidebar" class="grid_3" role="complementary">
    <header id="branding" role="banner">
        <a href="/${portalName}/" title=""/>
        <img src="/${portalName}/${portalTheme}/images/norvegiana.jpg" alt="Norvegiana"/>
        </a>
        <h1 class="large">${portalDisplayName}</h1>
    </header>
    
    <div id="facet-list">
        <#include "inc_related_content.ftl"/>
       <h5><@spring.message 'RelatedContent_t' />:</h5>
        <table summary="related items" id="tbl-related-items" width="100%">
            <#assign max=3/><!-- max shown in list -->
            <#list result.relatedItems as doc>
            <#if doc_index &gt; 2><#break/></#if>
            <tr>
                <td width="45" valign="top">
                    <div class="related-thumb-container">
                        <#if queryStringForPaging??>
                            <a href='${doc.fullDocUrl()}?query=europeana_uri:"${doc.id?url('utf-8')}"&amp;start=${doc.index()?c}&amp;view=${view}&amp;startPage=1&amp;pageId=brd&amp;tab='>
                         <#else>
                            <a href="${doc.fullDocUrl()}">
                         </#if>
                         <#if useCache="true">
                            <img src="${cacheUrl}uri=${doc.thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${doc.type}&amp;view=${view}" alt="Click here to view related item" width="40"/>
                        <#else>
                            <img src="${doc.thumbnail}" alt="Click here to view related item" width="40" onerror="showDefault(this,'${doc.type}')"/>
                        </#if>

                            </a>
                    </div>
                </td>

                <td class="item-titles" valign="top" width="130">
                    <#if queryStringForPaging??>
                    <a href='${doc.fullDocUrl()}?query=europeana_uri:"${doc.id?url('utf-8')}"&amp;start=${doc.index()?c}&amp;startPage=1&amp;pageId=brd'><@stringLimiter "${doc.title}" "50"/></a>
                    <#else>
                    <a href="${doc.fullDocUrl()}"><@stringLimiter "${doc.title}" "50"/></a>
                    </#if>
                </td>
            </tr>

            </#list>
            <#if result.relatedItems?size &gt; max>
            <tr>
                <td id="see-all" colspan="2"><a href='/${portalName}/brief-doc.html?query=europeana_uri:"${uri}"&amp;view=${view}'><@spring.message 'SeeAllRelatedItems_t' /></a></td>
            </tr>
            </#if>
        </table>

    </div>

</section>


<section id="item" class="grid_9" role="main">

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
            <h4><@resultsFullQueryBreadcrumbs/></h4>
        </div>
    </div>

    <div class="clear"></div>

    <nav class="pagination" role="navigation">
        <div class="inner">
        <@resultFullPagination/>
        </div>
    </nav>

    <div class="clear"></div>

    <div class="inner">

        <div id="itemImage" class="grid_4 alpha">
            <div class="inner">
            <@resultFullImage/>
            </div>
        </div>

        <div id="itemMetaData" class="grid_5 omega">
            <@resultFullList/>
        </div>

    </div>

</section>


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