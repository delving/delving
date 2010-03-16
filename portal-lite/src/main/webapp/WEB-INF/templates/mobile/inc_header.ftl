<#import "/spring.ftl" as spring >
<#assign useCache = "true">
<#if RequestParameters.useCache??>
    <#assign useCache = "${RequestParameters.useCache}"/>
</#if>
<#-- used for grabbing locale based includes and images -->
<#-- locale also used on homepage for language based announcements/disclaimers -->
<#assign locale = springMacroRequestContext.locale>
<#assign query = "">
<#assign cacheUrl = cacheUrl>
<#assign view = "mixed">
<#if RequestParameters.view??>
    <#assign view = "${RequestParameters.view}">
</#if>
<#if RequestParameters.query??>
    <#assign query = "${RequestParameters.query}">
</#if>


<#include "inc_macros.ftl">



<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<#-- favicon_red.ico is also available -->
    <link rel="shortcut icon" href="/portal/favicon_red.ico"/>
<#assign useJawr = false/>
    <!--- make sure to enable/disable (comment-out) the appropriate JAWR servlets in the web.xml -->
<#if useJawr >
    <link rel="stylesheet" type="text/css" href="css/jawr/common.css"/>
    <script type="text/javascript" src="js/jawr/global.js"></script>
    <#else>
        <#if pageId??>
            <#switch pageId>
                <#case "in">
                <#-- js_utilities needed for language selection -->
                    <script type="text/javascript" src="js/js_utilities.js"></script>
                <#break/>
                <#case "bd">
                <#-- needed for image placeholder: showDefaultSmall(...) -->
                    <script type="text/javascript" src="js/results.js"></script>
                <#break/>
            </#switch>
        </#if>
        <#if is_IEMobile?? && is_IEMobile = true>
            <script type="text/javascript" src="mobile/js/mobile.js"></script>
        </#if>

    <link rel="stylesheet" type="text/css" href="mobile/css/mobile.css"/>
</#if>
<#if device_screen_width??>
     <#if device_screen_height??>
         <meta name="viewport" content="width=${device_screen_width}, height=${device_screen_height}, user-scalable=no, initial-scale=1.0" />
     <#else>
        <meta name="MobileOptimized" content="${device_screen_width}">
     </#if>
</#if>

<meta name="HandheldFriendly" content="true" />

    <script type="text/javascript">
        var msgRequired = "<@spring.message 'RequiredField_t'/>";
    </script>
<#switch thisPage>
    <#case "index.html">
        <#assign pageId = "in"/>
        <#assign bodyId = "home"/>
        <title>Europeana - Homepage</title>
    <#break>
    <#case "brief-doc.html">
        <#assign pageId = "bd"/>
        <title>Europeana - Search results</title>
    <#break>
    <#case "full-doc.html">
        <#assign pageId = "fd"/>
        <title>Europeana - Search results</title>
    <#break>
    <#case "exception.html">
        <title>Europeana - Exception</title>
    <#break>
</#switch>
</head>

<body>



