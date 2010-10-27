<#import "spring.ftl" as spring >
<#--<#assign portalName = portalName/>-->

<#--<#assign portalTheme = portalName/>-->

<#-- used for grabbing locale based includes and images -->
<#-- locale also used on homepage for language based announcements/disclaimers -->
<#assign locale = springMacroRequestContext.locale>

<#assign cacheUrl = cacheUrl>

<#assign query = "">

<#if user??>
    <#assign user = user/>
</#if>

<#assign useCache = "true">

<#if RequestParameters.useCache??>
    <#assign useCache = "${RequestParameters.useCache}"/>
</#if>

<#assign view = "table">
<#if RequestParameters.view??>
    <#assign view = "${RequestParameters.view}">
</#if>

<#if RequestParameters.query??>
    <#assign query = "${RequestParameters.query}">
</#if>

<#if format??>
    <#assign format = format>
</#if>

<#assign interfaceLanguage = interfaceLanguage/>

<#include 'delving-macros.ftl'/>

<!DOCTYPE html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <script type="text/javascript">
        var msgRequired = "<@spring.message 'RequiredField_t'/>";
        var portalName = "/${portalName}";
        var baseThemePath = "/${portalName}/${portalTheme}";
    </script>
    <@addCss ["reset-text-grid.css","screen.css"], "screen"/>
    ${cssFiles}
    <@addJavascript ["jquery-1.4.2.min.js", "jquery.cookie.js", "js_utilities.js"]/>
    <#--
     * @addJavascript is called in the header to load the essential javascript files first.
     * Subsequent calls in underlying pages to @addJavascript append more files to the list
     * ${javascriptFiles} is placed in the footer -- best practice: load javascript last
     -->
</head>
<body>
<div class="container_12">
<@adminBlock/>
<div class="grid_12" id="userBar">
    <@userBar/>
    <#include "language_select.ftl"/>
</div>    
