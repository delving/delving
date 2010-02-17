<#import "spring.ftl" as spring />
<#assign query = query>
<#assign view="">
<#assign docList = docList>
<#assign cacheUrl = cacheUrl>
<#assign pagination = pagination>
<#assign startPage = startPage>
<#assign thisPage = "bob.html">
<#assign useCache = "true">
<#if RequestParameters.useCache??><#assign useCache = "${RequestParameters.useCache}"></#if>
<#macro stringLimiter theStr size>
    <#assign newStr = theStr>
    <#if newStr?length &gt; size?number>
        <#assign newStr = theStr?substring(0,size?number) + "...">
    </#if>
${newStr}
</#macro>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
    <title>BOB 0.1</title>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <link rel="stylesheet" title="Standard" href="css/common.css" type="text/css" media="screen"/>
    <link rel="stylesheet" title="Standard" href="css/bob.css" type="text/css" media="screen"/>
    <script type="text/javascript" language="javascript" src="js/imageflow.js"></script>
    <script>
        function showDefault(obj, iType) {
            switch (iType)
            {
                case "TEXT":
                    obj.src = "images/item-page-large.gif";
                    break;
                case "IMAGE":
                    obj.src = "images/item-image-large.gif";
                    break;
                case "VIDEO":
                    obj.src = "images/item-video-large.gif";
                    break;
                case "SOUND":
                    obj.src = "images/item-sound-large.gif";
                    break;
                default:
                    obj.src = "images/item-page-large.gif";
            }
        }
    </script>

</head>

<body>

<div id="imageflow">

    <noscript>
        <div class="attention">
        <@spring.message 'NoScript_t' />
        </div>
    </noscript>
    <h1>${query}</h1>

    <div id="loading">
        <b>Loading images</b><br/>
        <img src="images/loading.gif" width="208" height="13" alt="loading"/>
    </div>

    <div id="images">

    <#list docList as briefDoc>
        <#assign title><@stringLimiter "${briefDoc.getTitle()}" "80"/></#assign>
        <#if useCache="true">
            <img class="flow" src="${cacheUrl}type=${briefDoc.type}&amp;uri=${briefDoc.thumbnail}&amp;size=FULL_DOC"
                 longdesc='full-doc.html?uri=${briefDoc.id}&amp;start=${briefDoc.index?c}&amp;pageId=yg&amp;tab=&amp;startPage=${startPage}&amp;query=${query}'
                 alt="${title?html}"/>
            <#else>
                <img class="flow" src="${briefDoc.thumbnail}"
                     longdesc="full-doc.html?uri=${briefDoc.id}&amp;start=${briefDoc.index?c}&amp;pageId=yg&amp;startPage=${startPage}&amp;query=${query}"
                     alt="${title?html}" onerror="showDefault(this,'${briefDoc.type}');"/>
        </#if>


    </#list>

    </div>

    <div id="captions"></div>

    <div id="scrollbar">
        <div id="slider" style="left: 0px;"></div>
    </div>

    <div id="numinfo">
        results ${pagination.start} - ${pagination.lastViewableRecord} of ${pagination.numFound} <br/>
    <@resultnav />

    </div>

    <div id="objectPageing">

    </div>

</div>

</body>
</html>

<#macro resultnav>
    <#assign pagination = pagination>

<div class="nav">
    <ul>
        <#list pagination.pageLinks as link>
            <#if link.linked>
                <li><a href="${thisPage}?query=${query}&start=${link.start?c}&view=${view}">${link.display}</a></li>
                <#else>
                    <li><strong>${link.display}</strong></li>
            </#if>
        </#list>
        <#if pagination.previous>
            <li><a href="${thisPage}?query=${query}&start=${pagination.previousPage?c}&view=${view}"><img
                    src="images/arr-left.gif" hspace="5" width="9" height="7" alt="click for previous page of results"/></a>
            </li>
        </#if>
        <#if pagination.next>
            <li><a href="${thisPage}?query=${query}&start=${pagination.nextPage?c}&view=${view}"><img
                    src="images/arr-right.gif" hspace="5" width="9" height="7"
                    alt="Click here for next page of results"/></a></li>
        </#if>
    </ul>
</div>
</#macro>