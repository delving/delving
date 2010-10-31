<#compress>
    <#assign model = result/>
    <#assign result = result/>
    <#assign uri = result.fullDoc.id/>
    <#assign view = "table"/>
    <#assign thisPage = "full-doc.html"/>
    <#assign useCache = "true">
    <#if RequestParameters.useCache??>
        <#assign useCache = "${RequestParameters.useCache}"/>
    </#if>
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

    <#if RequestParameters.query??><#assign query = "${RequestParameters.query}"/></#if>

    <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
            "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
        <link rel="shortcut icon" href="/${portalName}/favicon.ico"/>
        <link rel="stylesheet" type="text/css" href="/${portalName}/${portalTheme}/css/reset-text-grid.css"/>
        <link rel="stylesheet" type="text/css" href="/${portalName}/${portalTheme}/css/jquery-ui-1.7.2.custom.css"/>
        <link rel="stylesheet" type="text/css" href="/${portalName}/${portalTheme}/css/layout-common.css"/>
    </head>

    <body>

    <div id="container" class="container_12">
        <div id="main" class="grid_9">

            <div id="item-detail">
                <#include "inc_result_table_full.ftl"/>
            </div>

        </div>

        <div class="grid_12" id="footer">
            <br/><br/>

        </div>

    </div>

    <script type="text/javascript" src="/${portalName}/${portalTheme}/js/jquery-1.4.1.js"></script>
    <script type="text/javascript" src="/${portalName}/${portalTheme}/js/jquery-ui-1.7.2.custom.js"></script>
    <script type="text/javascript" src="/${portalName}/${portalTheme}/js/js_utilities.js"></script>
    <script type="text/javascript" src="/${portalName}/${portalTheme}/js/results.js"></script>
    </body>
</html>

<#-- shorten a given string and append with ellipses -->
    <#macro stringLimiter theStr size>
        <#assign newStr = theStr>
        <#if newStr?length &gt; size?number>
            <#assign newStr = theStr?substring(0,size?number) + "...">
        </#if>
    ${newStr}
    </#macro>

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