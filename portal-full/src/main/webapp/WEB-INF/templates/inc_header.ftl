<#-- also change useCache in bob-new.ftl, it does not inherit it from the header -->
<#assign useCache = "true">
<#if RequestParameters.useCache??><#assign useCache = "${RequestParameters.useCache}"/></#if>
<#-- used for grabbing locale based includes and images -->
<#-- locale also used on homepage for language based announcements/disclaimers -->
<#assign locale = springMacroRequestContext.locale>
<#macro localeBased type toSwitch>
    <#-- if locale is something like en_us, then shorten to en -->
    <#if locale?length &gt; 2>
    <#assign locale =  locale?substring(0, 2)>
    </#if>
    <#if type == "include">
    <#-- current available languages -->
    <#assign langs = ["ca","cs","da","de","el","en","es","et","fi","fr","ga","hu","is","it","lt","lv","mt","nl","no","pl","pt","sk","sl","sv"]>
    <#if toSwitch == "inc_usingeuropeana_$$.ftl">
    <#-- current available languages for this particular file -->
    <#assign langs = ["de","en","es","fr"]>
    </#if>
    <#if langs?seq_index_of(locale) != -1>
    <#include "${toSwitch?replace('$$', locale)}">
    <#else>
    <#include "${toSwitch?replace('$$','en')}">
    </#if>
    <#else>
    <img src="images/${toSwitch?replace('$$',locale)!'think_culture_en_small.gif'}" alt=""/>
    </#if>
    </#macro>
    <#-- shorten a given string and append with ellipses -->
    <#macro stringLimiter theStr size>
    <#assign newStr = theStr>
    <#if newStr?length &gt; size?number>
    <#assign newStr = theStr?substring(0,size?number) + "...">
    </#if>
    ${newStr}
</#macro>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <#-- favicon_red.ico is also available -->
    <link rel="shortcut icon" href="/portal/favicon.ico" mce_href="favicon.ico"/>

    <script type="text/javascript">
        var msgRequired = "<@spring.message 'RequiredField_t'/>";
    </script>
    <#assign bypassJawr = debug>
    <#if bypassJawr = false >
        <link rel="stylesheet" type="text/css" href="css/bundles/global.css"/>
        <script type="text/javascript" src="js/lib/global.js"></script>
    <#else>
        <link rel="stylesheet" type="text/css" href="css/reset/reset.css"/>
        <link rel="stylesheet" type="text/css" href="css/fonts/fonts.css"/>
        <link rel="stylesheet" type="text/css" href="css/grids/grids.css"/>
        <link rel="stylesheet" type="text/css" href="css/ui.core.css"/>
        <link rel="stylesheet" type="text/css" href="css/ui.theme.css"/>
        <link rel="stylesheet" type="text/css" href="css/common.css"/>

        <script type="text/javascript" src="js/jquery-1.3.1.min.js"></script>
        <script type="text/javascript" src="js/jquery.toggleElements.pack.js"></script>
        <script type="text/javascript" src="js/jquery.cookie.js"></script>
        <script type="text/javascript" src="js/js_utilities.js"></script>
    </#if>
    <#switch thisPage>
        <#case "index.html">
            <#assign pageId = "in"/>
            <#assign bodyId = "home"/>
            <#if bypassJawr = false >
                <link rel="stylesheet" type="text/css" href="css/bundles/index.css"/>
                <script type="text/javascript" src="js/lib/validation.js"></script>
                <script type="text/javascript" src="js/lib/home.js"></script>
            <#else>

                <link rel="stylesheet" type="text/css" href="css/index.css"/>
                <link rel="stylesheet" type="text/css" href="css/jquery.jcarousel.css"/>
                <script type="text/javascript" src="js/jquery.jcarousel.pack.js"></script>
                <script type="text/javascript" src="js/index.js"></script>
            </#if>
            <title>Europeana - Homepage</title>
            <#break>
        <#case "advancedsearch.html">
                <#assign pageId = "adv"/>
                <#assign bodyId = "advancedsearch"/>
                <#if bypassJawr = false >
                    <link rel="stylesheet" type="text/css" href="css/bundles/index.css"/>
                    <script type="text/javascript" src="js/lib/home.js"></script>
                <#else>
                    <link rel="stylesheet" type="text/css" href="css/index.css"/>
                    <link rel="stylesheet" type="text/css" href="css/jquery.jcarousel.css"/>
                    <script type="text/javascript" src="js/jquery.jcarousel.js"></script>
                    <script type="text/javascript" src="/js/index.js"></script>
                </#if>
                <title>Europeana - Advanced Search</title>
                <#break>
        <#case "brief-doc.html">
            <#assign pageId = "bd"/>
            <#if user??>
            <script type="text/javascript">
                var msgSearchSaveSuccess = "<@spring.message 'SearchSaved_t'/>";
                var msgSearchSaveFail = "<@spring.message 'SearchSavedFailed_t'/>";
            </script>
            </#if>
            <#if bypassJawr = false >
                <link rel="stylesheet" href="css/bundles/results.css" type="text/css"/>
                <script type="text/javascript" src="js/lib/briefDoc.js"></script>
            <#else>
                <link rel="stylesheet" href="css/results.css" type="text/css"/>
                <link rel="stylesheet" href="css/facets.css" type="text/css"/>
                <script type="text/javascript" src="js/briefDoc.js"></script>
            </#if>
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
            <#if bypassJawr = false >
                <link rel="stylesheet" href="css/bundles/results.css" type="text/css"/>
                <script type="text/javascript" src="js/lib/validation.js"></script>
                <script type="text/javascript" src="js/lib/fullDoc.js"></script>
            <#else>
                <link rel="stylesheet" href="css/facets.css" type="text/css"/>
                <link rel="stylesheet" href="css/results.css" type="text/css"/>
                <link rel="stylesheet" href="css/results-detail.css" type="text/css"/>
                <script type="text/javascript" src="js/jquery.validate.js"></script>
                <script type="text/javascript" src="js/fullDoc.js"></script>
            </#if>
            <title>Europeana - Search results</title>
            <#break>
        <#case "myeuropeana.html">
            <#assign pageId = "me"/>
            <#if bypassJawr = false >
                <link rel="stylesheet" href="css/bundles/myeuropeana.css" type="text/css">
                <link rel="stylesheet" href="css/bundles/theme.css" type="text/css">
                <script type="text/javascript" src="js/lib/myeuropeana.js"></script>
                <script type="text/javascript" src="js/lib/tabs.js"></script>
            <#else>
                <link rel="stylesheet" href="css/myeuropeana.css" type="text/css">
                <link rel="stylesheet" href="css/ui.tabs.css" type="text/css">

                <script type="text/javascript" src="js/ui.core.js"></script>
                <script type="text/javascript" src="js/ui.tabs.js"></script>
                <script type="text/javascript" src="js/myEuropeana.js"></script>


            </#if>
            <title>Europeana - My Europeana</title>
            <#break>
        <#case "exception.html">
            <title>Europeana - Exception</title>
            <#break>
        <#case "login.html">
            <#if bypassJawr = false >
                <link rel="stylesheet" href="css/bundles/login-register.css" type="text/css"/>
                <script type="text/javascript" src="js/lib/validation.js"></script>
                <script type="text/javascript" src="js/lib/login.js"></script>
            <#else>
                <link rel="stylesheet" href="css/login-register.css" type="text/css"/>
                <script type="text/javascript" src="js/login.js"></script>
                <script type="text/javascript" src="/js/jquery.validate.js"></script>
            </#if>
            <title>Europeana - Login</title>
            <#break>
        <#case "logout.html">
            <#if bypassJawr = false >
                <link rel="stylesheet" href="css/bundles/login-register.css" type="text/css"/>
            <#else>
                <link rel="stylesheet" href="css/login-register.css" type="text/css"/>
            </#if>
            <title>Europeana - Logout</title>
            <#break>
        <#case "register.html">
            <#if bypassJawr = false >
                <link rel="stylesheet" href="css/bundles/login-register.css" type="text/css"/>
            <#else>
                <link rel="stylesheet" href="css/login-register.css" type="text/css"/>
            </#if>
            <title>Europeana - Registration</title>
            <#break>
        <#case "forgotPassword.html">
            `<#if bypassJawr = false >
                <link rel="stylesheet" href="css/bundles/login-register.css" type="text/css"/>
            <#else>
                <link rel="stylesheet" href="/css/login-register.css" type="text/css"/>
            </#if>
            <title>Europeana - Forgot Password</title>
            <#break>
        <#case "register-success.html">
            <#if bypassJawr = false >
                <link rel="stylesheet" href="css/bundles/login-register.css" type="text/css"/>
            <#else>
                <link rel="stylesheet" href="/css/login-register.css" type="text/css"/>
            </#if>
            <title>Europeana - Registration continued</title>
            <#break>
        <#case "year-grid.html">
            <#assign pageId = "yg">
            <#break>
        <#case "tag-grid.html">
            <#assign pageId = "tg">
            <#break>
        <#case "sitemap.html">
            <#break>
        <#case "thought-lab.html">
            <link rel="alternate" href="brief-doc.rss?query=mozart"  type="application/rss+xml" title="" id="gallery" />
            <script type="text/javascript" src="http://lite.piclens.com/current/piclens.js"></script>
            <title>Europeana - Thought lab</title>
            <#break>
        <#case "contact.html">
            <title>Europeana - Contact/Feedback</title>
            <#if bypassJawr = false >
                 <script type="text/javascript" src="js/lib/effects.js"></script>
                 <script type="text/javascript" src="js/lib/contact.js"></script>
            <#else>
                <script type="text/javascript" src="js/effects.core.js"></script>
                <script type="text/javascript" src="js/effects.blind.js"></script>
            </#if>

            <style type="text/css">
            .yui-g #contact { padding: 0 10px 0 0;}
            #contact h4 {border-bottom: 1px solid #eaeaea;}
            #contact p {margin: 4px 0 20px 0;}
            form {}
            fieldset {text-align: left;}
            legend {display:none;}
            label {}
            input.txt {border:1px solid #ccc; width: 50%;padding: 2px 0 0 4px;}
            textarea {width:100%; height: 200px;border:1px solid #ccc;}
            .error {font-size: 11px;margin-left: 4px;}
             p.success {background-color: green; color: #fff;padding: 4px}
            #effect { padding: 0.4em; position: relative; }
		    #effect h3 { margin: 0; padding: 0.4em; text-align: center; }
            </style>
            <#break>
        <#case "partners.html">
            <#assign pageId = "pa"/>
            <title>Europeana - Partners</title>
            <#break>
        <#case "aboutus.html">
            <#assign pageId = "au"/>
            <title>Europeana - About us</title>
            <#if bypassJawr = false >
                <link rel="stylesheet" href="css/bundles/theme.css" type="text/css"/>
            <#else>
                <link rel="stylesheet" href="css/ui.theme.css" type="text/css"/>
                <link rel="stylesheet" href="css/ui.core.css" type="text/css"/>
                <link rel="stylesheet" href="css/ui.tabs.css" type="text/css"/>
            </#if>

            <#break>
        <#case "thinkvideo.html">
            <title>Europeana - video storyline and credits</title>
            <#break>
    </#switch>
    <#if bypassJawr = false >
        <!--[if gte IE 6]>
        <link rel="stylesheet" type="text/css" href="css/bundles/ie6.css" />
        <![endif]-->
        <!--[if gte IE 7]>
        <link rel="stylesheet" type="text/css" href="css/bundles/ie7.css" />
        <![endif]-->
    <#else>
        <!--[if gte IE 6]>
        <link rel="stylesheet" type="text/css" href="css/ie6.css" />
        <![endif]-->
        <!--[if gte IE 7]>
        <link rel="stylesheet" type="text/css" href="css/ie7.css" />
        <![endif]-->
    </#if>
</head>
