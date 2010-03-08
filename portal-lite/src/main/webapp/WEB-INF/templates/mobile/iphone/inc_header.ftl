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


<#include "../inc_macros.ftl">



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

        <meta name="apple-mobile-web-app-capable" content="yes"/>
        <meta name="viewport" content="minimum-scale=1.0, width=device-width, maximum-scale=1.0, user-scalable=no"/>
        <link rel="apple-touch-startup-image" href="mobile/images/apple-startup.png"/>
        <link rel="apple-touch-icon" href="mobile/images/apple-touch-icon.png"/>

        <!-- iwebkit v5.x -->
        <link rel="stylesheet" type="text/css" href="mobile/iwebkit/css/style.css"/>
        <link rel="stylesheet" type="text/css" href="mobile/css/iwebkit_addon.css"/>
        <script type="text/javascript" src="mobile/iwebkit/javascript/functions.js"></script>

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
</#if>

    <script type="text/javascript">
        var msgRequired = "<@spring.message 'RequiredField_t'/>";
    </script>
<#switch thisPage>
    <#case "index.html">
        <#assign pageId = "in"/>
        <#assign bodyId = "home"/>
        <#include "../inc_flow.ftl">
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

<!-- head end -->
<!-- body start -->
<#if pageId??>
    <#switch pageId>
        <#case "in">
        <body onload="initflow();">
            <#break/>
        <#case "bd">
        <#case "fd">
        <body class="list">
            <#break/>
        <#default>
        <body>
            <#break/>
    </#switch>
</#if>

<#if pageId??>
    <#if pageId == "bd"> <#-- on the result page we use the title bar to switch between display modes: no caption in the title bar-->
        <div id="topbar">
    <#else>
        <div id="topbar">
            <div id="title">Europeana</div>
    </#if>

    <#if pageId != "in">
        <#if pageId == "bd">
            <div id="leftnav"><a href="index.html"><img alt="home" src="mobile/iwebkit/images/home.png"/></a></div>
            <#elseif pageId == "fd">
                <div id="leftnav">
                <#-- on the full doc, we provide a link back to the result page with the appropriate icon -->
                    <#if pagination?? && pagination.returnToResults?? && view?? && query?? && query?length &gt; 0 >
                        <a href="${pagination.returnToResults?html}">
                            <#switch view>
                                <#case "text_only"><img class="titleresultnav"
                                                        src="mobile/images/text_only.gif"/><#break/>
                                <#case "image_only"><img class="titleresultnav"
                                                         src="mobile/images/image_only.gif"/><#break/>
                                <#default><#case "mixed"><img class="titleresultnav"
                                                              src="mobile/images/mixed.gif"/><#break/>
                            </#switch>
                        </a>
                    </#if>
                    <a href="index.html"><img alt="home" src="mobile/iwebkit/images/home.png"/></a>
                </div>
        </#if>
        <#else>
        </div>
    </#if>
    <#else>
    </div>
</#if>