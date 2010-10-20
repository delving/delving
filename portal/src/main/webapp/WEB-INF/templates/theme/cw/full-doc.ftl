<#import "spring.ftl" as spring />
<#assign model = result/>
<#assign result = result/>
<#assign uri = result.fullDoc.id/>
<#assign view = "table"/>
<#assign socialTags = socialTags/>
<#assign thisPage = "full-doc.html"/>
<#compress>
    <#if RequestParameters.query??><#assign query = "${RequestParameters.query}"/></#if>
    <#include "inc_header.ftl">
    <#if startPage??><#assign startPage = startPage/></#if>
    <#if RequestParameters.view??> <#assign view = "${RequestParameters.view}"/></#if>
    <#if format??><#assign format = format/></#if>
    <#if pagination??>
        <#assign pagination = pagination/>
        <#assign queryStringForPaging = pagination.queryStringForPaging />
    </#if>
    <#if queryStringForPaging??>
        <#assign defaultQueryParams = "${portalName}/record/${result.fullDoc.id}.html?"+queryStringForPaging+"&start="+pagination.docIdWindow.offset?c+"&view="+view />
        <#else>
            <#assign defaultQueryParams = "${portalName}/record/${result.fullDoc.id}.html" />
    </#if>
    <#if result.fullDoc.dcTitle[0]?length &gt; 110>
        <#assign postTitle = result.fullDoc.dcTitle[0]?substring(0, 110)?url('utf-8') + "..."/>
        <#else>
            <#assign postTitle = result.fullDoc.dcTitle[0]?url('utf-8')/>
    </#if>
    <#if result.fullDoc.dcCreator[0]?matches(" ")>
        <#assign postAuthor = "none"/>
        <#else>
            <#assign postAuthor>
                <@stringLimiter "${result.fullDoc.dcCreator[0]}" "75"/>
            </#assign>
    </#if>
<#-- Removed ?url('utf-8') from query assignment -->



<div id="main">



    <div id="left-col" class="grid_3">

        <div class="breadcrumb">
            <em>U bevindt zich op: </em>
            <span>
                <a href="/${portalName}/index.html" title="Homepagina">Home</a>
                <span class="imgreplacement">&rsaquo;</span>
                <#if pagination??>
                    <a href="${pagination.returnToResults?html}" title="Zoekresultaten">Zoekresultaten</a>
                    <span class="imgreplacement">&rsaquo;</span>
                </#if>
            </span>
            Object
        </div>

        <#include "inc_related_content.ftl"/>

    </div>

    <div id="right-col" class="grid_9">

        <div id="search">
            <@SearchForm "search_result"/>
        </div>

        <div class="clear"></div>

        <#if pagination??>

        <div id="query_breadcrumbs">

            <dl>
                <dt><@spring.message 'MatchesFor_t' />:</dt>
                <#if !query?starts_with("europeana_uri:")>
                    <#list pagination.breadcrumbs as crumb>
                    <#assign crumbFriendlyName = crumb.display?replace("YEAR:","")?replace("COLLECTION:"," ")?replace("DCTYPE:","")?html/>
                        <#if !crumb.last>
                            <dd <#if crumb_index == 0>class="nobg"</#if>><a href="/${portalName}/brief-doc.html?${crumb.href}">${crumbFriendlyName}</a></dd>
                        <#else>
                            <dd <#if crumb_index == 0>class="nobg"</#if>>${crumbFriendlyName}</dd>
                        </#if>
                    </#list>
                 <#else>
                    <dd class="nobg">

                    <@spring.message 'ViewingRelatedItems_t' />
                        <#assign match = result.fullDoc />
                        <a href="${portalName}/record/${match.id}.html">
                            <#if useCache="true"><img
                                    src="${cacheUrl}uri=${match.thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${match.type}"
                                    alt="${match.title}" height="25"/>
                                <#else><img src="${match.thumbnail}" alt="${match.title}" height="25"/>
                            </#if>
                        </a>

                    </dd>
                </#if>
            </dl>

        </div>

        </#if>
            
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

                                 <#assign UrlRef = "#"/>
                                <#if !result.fullDoc.europeanaIsShownAt[0]?matches(" ")>
                                    <#assign UrlRef = result.fullDoc.europeanaIsShownAt[0]/>
                                <#elseif !result.fullDoc.europeanaIsShownBy[0]?matches(" ")>
                                    <#assign UrlRef = result.fullDoc.europeanaIsShownBy[0]/>
                                </#if>
                                <a
                                    href="/${portalName}/redirect.html?shownAt=${UrlRef?url('utf-8')}&provider=${result.fullDoc.europeanaProvider[0]}&id=${result.fullDoc.id}"
                                    target="_blank"
                                    alt="<@spring.message 'ViewInOriginalContext_t' /> - <@spring.message 'OpensInNewWindow_t'/>"
                                    title="<@spring.message 'ViewInOriginalContext_t' /> - <@spring.message 'OpensInNewWindow_t'/>"
                                    class="fg-button ui-state-default fg-button-icon-left ui-corner-all"
                                >
                                    <span class="ui-icon ui-icon-newwin"></span><@spring.message 'ViewInOriginalContext_t' />
                                </a>

            </#if>

        </div>

        <div class="clear"></div>

        <div id="item-detail">
            <#include "inc_result_table_full.ftl"/>
        </div>

        <div class="clear"></div>
    </div>

</div>
<script type="text/javascript">
    $(document).ready(function(){
        $("a.overlay").fancybox({
            titleShow   : true,
            titlePosition: 'inside'
        });
    })
</script>
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
                <@simple_list values1 separator />
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