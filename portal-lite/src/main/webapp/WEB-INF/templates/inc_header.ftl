<#import "spring.ftl" as spring >
<#if user??>
    <#assign user = user/>
</#if>
<#assign useCache = "true">
<#if RequestParameters.useCache??>
    <#assign useCache = "${RequestParameters.useCache}"/>
</#if>
<#-- used for grabbing locale based includes and images -->
<#-- locale also used on homepage for language based announcements/disclaimers -->
<#assign locale = springMacroRequestContext.locale>
<#assign query = "">
<#assign cacheUrl = cacheUrl>
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

<#macro SearchForm className>

    <#assign showAdv="none"/>
    <#assign showSim="block"/>
    <#if pageId??>
        <#if pageId=="adv">
            <#assign showAdv="block"/>
            <#assign showSim="none"/>
        </#if>
    </#if>

    <div id="search_simple" class="${className}" style="display:${showSim};">
        <#if result?? >
            <#if result.badRequest?? >
                <span style="font-style: italic;">Wrong query. ${result.errorMessage}</span>
            </#if>
        </#if>
        <form method="get" action="brief-doc.html" accept-charset="UTF-8" onsubmit="return checkFormSimpleSearch('query');">
            <input type="hidden" name="start" value="1" />
            <input type="hidden" name="view" value="${view}" />
            <input class="txt-input" name="query" id="query" type="text" title="Europeana Search" maxlength="75" />
            <input id="submit_search" type="submit" value="<@spring.message 'Search_t' />" />
            <a href="advancedsearch.html" id="href-advanced" title="<@spring.message 'AdvancedSearch_t' />"><@spring.message 'AdvancedSearch_t' /></a>
        </form>
    </div>

    <div id="search_advanced" class="${className}" style="display:${showAdv};" title="<@spring.message 'AdvancedSearch_t' />">
       <form method="get" action="brief-doc.html" accept-charset="UTF-8">
        <input type="hidden" name="start" value="1" />
        <input type="hidden" name="view" value="${view}" />
        <table>
            <tr>
                <td>&#160;</td>
                <td><select name="facet1" id="facet1"><option value=""><@spring.message 'AnyField_t'/> &nbsp;</option><option value="title"><@spring.message 'Title_t'/></option><option value="creator"><@spring.message 'Creator_t'/></option><option value="date"><@spring.message 'Date_t'/></option><option value="subject"><@spring.message 'Subject_t'/></option></select></td>
                <td><input type="text" name="query1" class="search-input" maxlength="75"/></td>
            </tr>
            <tr>
                <td align="right"><select name="operator2" id="operator2"><option value="and"><@spring.message 'AndBoolean_t'/> &nbsp;</option><option value="or"><@spring.message 'OrBoolean_t'/> </option><option value="not"><@spring.message 'NotBoolean_t'/> </option></select></td>
                <td><select name="facet2" id="facet2"><option value=""><@spring.message 'AnyField_t'/> &nbsp;</option><option value="title"><@spring.message 'Title_t'/></option><option value="creator"><@spring.message 'Creator_t'/></option><option value="date"><@spring.message 'Date_t'/></option><option value="subject"><@spring.message 'Subject_t'/></option></select></td>
                <td><input type="text" name="query2" class="search-input" maxlength="75"/></td>
            </tr>
            <tr>
                <td align="right"><select name="operator3" id="operator3"><option value="and"><@spring.message 'AndBoolean_t'/> &nbsp;</option><option value="or"><@spring.message 'OrBoolean_t'/> </option><option value="not"><@spring.message 'NotBoolean_t'/> </option></select></td>
                <td><select name="facet3" id="facet3"><option value=""><@spring.message 'AnyField_t'/> &nbsp;</option><option value="title"><@spring.message 'Title_t'/></option><option value="creator"><@spring.message 'Creator_t'/></option><option value="date"><@spring.message 'Date_t'/></option><option value="subject"><@spring.message 'Subject_t'/></option></select></td>
                <td><input type="text" name="query3" class="search-input" maxlength="75"/></td>
            </tr>
            <tr>
                <td colspan="3">&#160;</td>
            </tr>
            <tr>
                <td align="left"><input type="reset" value="<@spring.message 'Reset_t' />" /></td>
                <td>&#160;</td>
                <td align="right"><input id="searchsubmit2" type="submit" value="<@spring.message 'Search_t' />" /></td>
            </tr>
         </table>
        </form>
    </div>
</#macro>

<#macro userbar>
    <ul>
        <#if !user??>
        <li id="mustlogin" class="msg"><a href="login.html?pId=${pageId}"><u><@spring.message 'LogIn_t'/></u></a> | <a
                href="login.html?pId=${pageId}"><u><@spring.message 'Register_t'/></u></a>
        </li>
        </#if>
        <#if user??>
        <li>
            <@spring.message 'LoggedInAs_t' />: <strong>${user.userName?html}</strong> | <a
                href="logout.html"><@spring.message 'LogOut_t' /></a>
        </li>
        <#if user.savedItems?exists>
        <li>
            <a href="myeuropeana.html" onclick="$.cookie('ui-tabs-3', '1', { expires: 1 });">
                <@spring.message 'SavedItems_t' />
            </a>
            (<span id="savedItemsCount">${user.savedItems?size}</span>)
        </li>
        </#if>
        <#if user.savedSearches?exists>
        <li>
            <a href="myeuropeana.html" onclick="$.cookie('ui-tabs-3', '2', { expires: 1 });">
                <@spring.message 'SavedSearches_t' />
            </a>
            (<span id="savedSearchesCount">${user.savedSearches?size}</span>)
        </li>
        </#if>
        <#if user.socialTags?exists>
        <li>
            <a href="myeuropeana.html" onclick="$.cookie('ui-tabs-3', '3', { expires: 1 });">
                <@spring.message 'SavedTags_t' />
            </a>
            (<span id="savedTagsCount">${user.socialTags?size}</span>)
        </li>
        </#if>
        </#if>
    </ul>
</#macro>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <#-- favicon_red.ico is also available -->
    <link rel="shortcut icon" href="/portal/favicon.ico"/>
    <link rel="stylesheet" type="text/css" href="css/reset-text-grid.css"/>
    <link rel="stylesheet" type="text/css" href="css/jquery-ui-1.7.2.custom.css"/>
    <link rel="stylesheet" type="text/css" href="css/layout-common.css"/>

    <script type="text/javascript" src="js/jquery-1.4.1.js"></script>
    <script type="text/javascript" src="js/jquery-ui-1.7.2.custom.js"></script>
    <script type="text/javascript" src="js/jquery.cookie.js"></script>
    <script type="text/javascript" src="js/jquery.toggleElements.js"></script>
    <script type="text/javascript" src="js/jquery.validate.js"></script>
    <script type="text/javascript" src="js/js_utilities.js"></script>
    <script type="text/javascript" src="js/results.js"></script>
    <script type="text/javascript" src="js/myEuropeana.js"></script>
    <script type="text/javascript">
        var msgRequired = "<@spring.message 'RequiredField_t'/>";
    </script>
    <#switch thisPage>
    <#case "index.html">
    <#assign pageId = "in"/>
    <#assign bodyId = "home"/>
    <title>Open-Europeana - Homepage</title>
    <#break>
    <#case "advancedsearch.html">
    <#assign pageId = "adv"/>
    <#assign bodyId = "advancedsearch"/>
    <#--<script type="text/javascript" src="js/lib/home.js"></script>-->
    <title>Open-Europeana - Advanced Search</title>
    <#break>
    <#case "brief-doc.html">
    <#assign pageId = "brd"/>
    <script type="text/javascript">
        var msgSearchSaveSuccess = "<@spring.message 'SearchSaved_t'/>";
        var msgSearchSaveFail = "<@spring.message 'SearchSavedFailed_t'/>";

        $(document).ready(function() {
            $('div.toggler-c').toggleElements(
            { fxAnimation:'slide', fxSpeed:'fast', className:'toggler' });
        });

    </script>
    <title>Open-Europeana - Search results</title>
    <#break>
    <#case "full-doc.html">
    <#assign pageId = "fd"/>

    <#if user??>
    <script type="text/javascript">
        var msgItemSaveSuccess = "<@spring.message 'ItemSaved_t' />";
        var msgItemSaveFail = "<@spring.message 'ItemSaveFailed_t' />";
        var msgEmailSendSuccess = "<@spring.message 'EmailSent_t' />";
        var msgEmailSendFail = "<@spring.message 'EmailSendFailed_t' />";
        var msgEmailValid = "<@spring.message 'EnterValidEmail_t' />";
    </script>
    </#if>
    <title>Open-Europeana - Search results</title>
    <#break>
    <#case "myeuropeana.html">
    <#assign pageId = "me"/>
    <script type="text/javascript">
        $(document).ready(function() {
            $("#savedItems").tabs('select', $.cookie('ui-tabs-3'));
            $("#savedItems").tabs({ cookie: { expires: 30 } });
        });
    </script>
    <title>Open-Europeana - My Open-Europeana</title>
    <#break>
    <#case "exception.html">
    <title>Europeana - Exception</title>
    <#break>
    <#case "login.html">
    <#assign pageId = "li"/>
    <script type="text/javascript">
        $(document).ready(function() {
            $("#loginForm").validate({
                rules: {j_username: "required",j_password: "required"},
                messages: {j_username: "",j_password: ""}
            });
            $("#forgotemailForm").validate({
                rules: {email: "required"},
                messages: {email: ""}
            });
            $("#registrationForm").validate({
                rules: {email: "required",iagree: "required"},
                messages: {email: "",iagree: msgRequired }
                //msgRequired is generated in inc_header.ftl and
                // set as a javascript variable (its a spring message
                // so cannot be generated in this js file
            });
        });
    </script>
    <title>Open-Europeana - Login</title>
    <#break>
    <#case "logout.html">
    <#assign pageId = "lo"/>
    <title>Open-Europeana - Logout</title>
    <#break>
    <#case "register.html">
    <#assign pageId = "rg"/>
    <title>Open-Europeana - Registration</title>
    <#break>
    <#case "forgotPassword.html">
    <#assign pageId = "fp"/>
    <title>Open-Europeana - Forgot Password</title>
    <#break>
    <#case "register-success.html">
    <title>Open-Europeana - Registration continued</title>
    <#break>
    </#switch>

</head>

<body>

<div id="container" class="container_12">