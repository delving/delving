<#-- also change useCache in bob-new.ftl, it does not inherit it from the header -->
<#assign useCache = "true">
<#if RequestParameters.useCache??><#assign useCache = "${RequestParameters.useCache}"/></#if>
<#-- used for grabbing locale based includes and images -->
<#-- locale also used on homepage for language based announcements/disclaimers -->
<#assign locale = springMacroRequestContext.locale>

<#assign  pageId = ""/>
<#-- shorten a given string and append with ellipses -->

<#macro stringLimiter theStr size>
    <#assign newStr = theStr>
    <#if newStr?length &gt; size?number>
        <#assign newStr = theStr?substring(0,size?number) + "...">
    </#if>
    ${newStr}
</#macro>
<#if thisPage?contains("full-doc.html")>
    <!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML+RDFa 1.0//EN" "http://www.w3.org/MarkUp/DTD/xhtml-rdfa-1.dtd">
    <html xmlns="http://www.w3.org/1999/xhtml" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#">
<#else>
    <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    <html xmlns="http://www.w3.org/1999/xhtml">
</#if>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <#-- favicon_red.ico is also available -->
    <link rel="shortcut icon" href="/portal/favicon.ico"/>

    <script type="text/javascript">
        var msgRequired = "<@spring.message 'RequiredField_t'/>";
    </script>
    	<link rel="stylesheet" type="text/css" href="css/epf-common.css"/>
    	<script type="text/javascript" src="js/jquery-1.4.1.js"></script>
    	<script type="text/javascript" src="js/jquery.toggleElements.js"></script>
    	<script type="text/javascript" src="js/jquery.cookie.js"></script>
    	<script type="text/javascript" src="js/js_utilities.js"></script>
    <#switch thisPage>
        <#case "index.html">
            <#assign pageId = "in"/>
               	<link rel="stylesheet" type="text/css" href="css/jquery.jcarousel.css"/>
            	<script type="text/javascript" src="js/jquery.jcarousel.js"></script>
            	<script type="text/javascript" src="js/index.js"></script>
                <title>Europeana - Homepage</title>
            <#break>
        <#case "advancedsearch.html">
                <#assign pageId = "adv"/>
                <#assign bodyId = "advancedsearch"/>
                    <link rel="stylesheet" type="text/css" href="css/index.css"/>
                    <link rel="stylesheet" type="text/css" href="css/jquery.jcarousel.css"/>
                    <script type="text/javascript" src="js/jquery.jcarousel.js"></script>
                    <script type="text/javascript" src="js/index.js"></script>
                    <title>Europeana - Advanced Search</title>
                <#break>
        <#case "brief-doc.html">
            <#assign pageId = "brd"/>
            <#if user??>
            <script type="text/javascript">
                var msgSearchSaveSuccess = "<@spring.message 'SearchSaved_t'/>";
                var msgSearchSaveFail = "<@spring.message 'SearchSavedFailed_t'/>";
            </script>
            </#if>
                <script type="text/javascript" src="js/results.js"></script>
                <title>Europeana - Search results</title>
            <#break>
        <#case "full-doc.html">
        <#assign pageId = "fd"/>
            <#if user??>
            <script type="text/javascript">
                var msgItemSaveSuccess = "<@spring.message 'ItemSaved_t' />";
                var msgItemSaveFail = "<@spring.message 'ItemSaveFailed_t' />";
                var msgEmailSendSuccess = "<@spring.message 'EmailSent_t' />";
                var msgEmailSendFail  = "<@spring.message 'EmailSendFailed_t' />";
                var msgEmailValid = "<@spring.message 'EnterValidEmail_t' />";
            </script>
            </#if>
                <script type="text/javascript" src="js/jquery.validate.js"></script>
                <script type="text/javascript" src="js/results.js"></script>
                <title>Europeana - Search results</title>
            <#break>
        <#case "myeuropeana.html">
            <#assign pageId = "me"/>
                <link rel="stylesheet" href="css/myeuropeana.css" type="text/css">
                <script type="text/javascript" src="js/ui.core.js"></script>
                <script type="text/javascript" src="js/ui.tabs.js"></script>
                <script type="text/javascript" src="js/myEuropeana.js"></script>
                <title>Europeana - My Europeana</title>
            <#break>
        <#case "exception.html">
            <title>Europeana - Exception</title>
            <#break>
        <#case "login.html">
            <#assign pageId = "li"/>
                <link rel="stylesheet" href="css/login-register.css" type="text/css"/>
                <script type="text/javascript" src="js/login.js"></script>
                <script type="text/javascript" src="js/jquery.validate.js"></script>
                <title>Europeana - Login</title>
            <#break>
        <#case "logout.html">
                <#assign  pageId = "lo"/>
               <link rel="stylesheet" href="css/login-register.css" type="text/css"/>
                <title>Europeana - Logout</title>
            <#break>
        <#case "register.html">
                <#assign  pageId = "rg"/>
                <link rel="stylesheet" href="css/login-register.css" type="text/css"/>
                <title>Europeana - Registration</title>
            <#break>
        <#case "forgotPassword.html">
                <#assign  pageId = "fp"/>
                <link rel="stylesheet" href="css/login-register.css" type="text/css"/>
                <title>Europeana - Forgot Password</title>
            <#break>
        <#case "register-success.html">
                 <#assign  pageId = "rs"/>
                <link rel="stylesheet" href="css/login-register.css" type="text/css"/>
                <title>Europeana - Registration continued</title>
            <#break>
        <#case "year-grid.html">
            <#assign pageId = "yg">
            <#break>
        <#case "tag-grid.html">
            <#assign pageId = "tg">
            <#break>
        <#case "sitemap.html">
            <#assign pageId = "sm">
        <title>Europeana - Sitemap</title>
            <#break>
        <#case "thought-lab.html">
            <link rel="alternate" href="brief-doc.rss?query=mozart"  type="application/rss+xml" title="" id="gallery" />
            <script type="text/javascript" src="http://lite.piclens.com/current/piclens.js"></script>
            <title>Europeana - Thought lab</title>
            <#break>
        <#case "contact.html">
            <#assign bodyId = "contact"/>
                <#assign  pageId = "co"/>
                <title>Europeana - Contact/Feedback</title>
                <script type="text/javascript" src="js/contact.js"></script>
                <link rel="stylesheet" href="css/contact.css" type="text/css"/>
            <#break>
        <#case "partners.html">
            <#assign pageId = "pa"/>
            <title>Europeana - Partners</title>
            <link rel="stylesheet" href="css/partners.css" type="text/css"/>
            <#break>
        <#case "aboutus.html">
            <#assign pageId = "au"/>
            <title>Europeana - About us</title>
             <#break>
        <#case "thinkvideo.html">
            <title>Europeana - video storyline and credits</title>
            <#break>
    </#switch>

        <!--[if gte IE 6]>
        <link rel="stylesheet" type="text/css" href="css/ie6.css" />
        <![endif]-->
        <!--[if gte IE 7]>
        <link rel="stylesheet" type="text/css" href="css/ie7.css" />
        <![endif]-->

</head>
<body id="${pageId}">
