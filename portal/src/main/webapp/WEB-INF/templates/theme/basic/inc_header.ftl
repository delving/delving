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
</head>
<body>