<#if user??>
    <#assign user = user/>
</#if>
<#--<#import "spring.ftl" as spring >-->
<#import "spring_form_macros.ftl" as spring />

<#-- GLOBAL ASSIGNS -->
<#assign locale = springMacroRequestContext.locale>

<#assign view = "table"/>
<#if RequestParameters.view??>
    <#assign view = RequestParameters.view/>
</#if>
<#assign useCache = "true">
<#assign javascriptFiles = ""/>
<#assign cssFiles = ""/>
<#assign contentOnly = "false"/>
<#if RequestParameters.contentOnly??>
    <#assign contentOnly = RequestParameters.contentOnly/>
</#if>
<#if !thisPage??>
    <#assign thisPage = ""/>
</#if>



<#--getSearchParams: <#list queryParamList.getSearchParams() as qp>${qp.key()}=${qp.getFirst()}&</#list><br/>-->

<#--getSearchParamsFormatted(): ${queryParamList.getSearchParamsFormatted()}<br/>-->

<#--getDefaultParams(): <#list queryParamList.getDefaultParams() as qp>${qp.key()}=${qp.getFirst()}&</#list><br/>-->

<#--getDefaultParamsFormatted(): ${queryParamList.getDefaultParamsFormatted()}<br/>-->

<#--getList(): <#list queryParamList.getList() as qp>${qp.key()}=${qp.getFirst()}&</#list><br/>-->

<#--getListFormatted(): ${queryParamList.getListFormatted()} <br/>-->

<#--getListFiltered(true, ['query','view','tab']):<#list queryParamList.getListFiltered(true, ['view', 'tab', 'orderBy']) as qp> ${qp.key()}=${qp.getFirst()}</#list><br/>-->

<#--getListFiltered(false, ['qf']):<#list queryParamList.getListFiltered(false, ['qf']) as qp> ${qp.key()}=${qp.getFirst()}</#list><br/>-->


<#--
 * adminBlock
 *
 * Macro to generate an administrative block.
 * Only viewabe for role 'administrator' or 'god'
 -->
<#macro adminBlock>
    <#if user?? && (user.role == ('ROLE_ADMINISTRATOR') || user.role == ('ROLE_GOD'))>
    <section id="adminBlock">
        <ul id="admin-menu">
            <li><a class="<#if thisPage == 'static-page.dml'>active</#if>" href="/${portalName}/_.dml"><@spring.message '_cms.administration.pages' /></a></li>
            <li><a class="<#if thisPage == 'static-image.dml'>active</#if>" href="/${portalName}/_.img"><@spring.message '_cms.administration.images' /></a></li>
            <li><a class="<#if thisPage == 'administration.html'>active</#if>" href="/${portalName}/administration.html"><@spring.message '_cms.administration.users' /></a></li>
            <li><a class="<#if thisPage == 'statistics.html'>active</#if>" href="/${portalName}/statistics.html"><@spring.message '_portal.ui.statistics' /></a></li>
        </ul>
    </section>

    <div class="clear"></div>
    </#if>
</#macro>

<#macro addJavascript fileList>
    <#if fileList??>
        <#if fileList?size &gt; 0>
             <#list fileList as file>
                 <#assign javascriptFiles>${javascriptFiles}${"\r"}<script type="text/javascript" src="/${portalName}/${portalTheme}/js/${file}"></script></#assign>
             </#list>
        </#if>
    </#if>
</#macro>

<#--
 * addCss
 *
 * generates the html for linked css pages
 * @param media : "screen" or "print" or "all"
 -->
<#macro addCss fileList media="all">
    <#if fileList??>
        <#if fileList?size &gt; 0>
             <#list fileList as file>
                 <#--<link rel="stylesheet" type="text/css" href="/${portalName}/${portalTheme}/css/${file}" media="${media}"/>-->
                 <link rel="stylesheet" type="text/css" href="/${portalName}/${portalTheme}/css/${file}"/>
             </#list>
        </#if>
    </#if>
</#macro>

<#--
 * addHeader
 *
 * generates the html header for the page
 * @param title : title of the page
 * @param bodyClass : class for the body tag (for css name-spacing for different color profiles
 * @param pageCssFiles : additional css files appended to the default
 * @param pageJsFiles : additional js files appended to the default
 -->
<#macro addHeader title="${portalDisplayName}" bodyClass="" pageJsFiles=[] pageCssFiles=[]>
    <#if contentOnly != "true">
        <!DOCTYPE html>
        <head>
            <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
            <title>${title}</title>
            <script type="text/javascript">
                var locale = "${locale}";
                var msgRequired = "<@spring.message '_mine.user.register.requiredfield'/>";
                var portalName = "/${portalName}";
                var baseThemePath = "/${portalName}/${portalTheme}";
            </script>
            <@addCss ["reset.css","text.css","960.css","jquery-ui-1.8.7.custom.css","screen.css"], ""/>
            <#if pageCssFiles?size &gt; 0>
                <@addCss pageCssFiles/>
            </#if>
            ${cssFiles}
            <!--[if lte IE 9]>
            <script src="/${portalName}/${portalTheme}/js/html5.js" type="text/javascript"></script>
            <![endif]-->
            <@addJavascript ["jquery-1.4.2.min.js","jquery.validate.pack.js","jquery-ui-1.8.5.custom.min.js","jquery.cookie.js", "js_utilities.js"]/>
            <#if (pageJsFiles?size &gt; 0)>
                <@addJavascript pageJsFiles/>
            </#if>
            ${javascriptFiles}
        </head>
        <body class="${bodyClass}">

          <div class="container_12">

            <@adminBlock/>

            <div class="grid_5 prefix_3">
                <@showMessages/>
            </div>      

            <div id="header" <#if (thisPage?? && thisPage=="index.html")>class="home"</#if>>

                <div id="userBar" role="navigation">
                    <#include "language_select.ftl"/><@userBar/>
                </div>

                <h1 id="branding">
                    <a id="" href="/${portalName}/" alt="Home" class="grid_3">
                    ${portalDisplayName}
                    </a>
                </h1>                

                <div id="search" class="grid_9 ui-corner-all">
                    <@simpleSearch/>
                    <noscript>
                        <@spring.message '_portal.ui.message.noscript' />
                    </noscript>

                    <#if (thisPage?? && thisPage=="index.html")>
                        <p id="intro-text"></p>
                    </#if>
                </div>
            </div>

            <div class="clear"></div>


    </#if>
</#macro>

<#macro addHtmlFooter>
    </body>
</html>
</#macro>

<#macro showMessages>
    <div id="messages">
        <div class="inner">
            <div class="message"></div>
            <div class="actions"><a href="#!" onclick="javascript:$('#messages').slideUp('slow');">Close</a></div>
        </div>
    </div>
</#macro>

<#macro addFooter >
    <#if contentOnly!="true">   

        <div class="clear"></div>
        <footer>
            <div class="inner">
                <div id="footer-dynamic-content"></div>
            </div>
        </footer>

        </div><#-- // container_12 -->


        <script type="text/javascript">
            delvingPageCall("#footer-dynamic-content", portalName+"/footer.dml?embedded=true"," "," "," ");
        </script>
 
        <#if trackingCode??>
            <script type="text/javascript">

          var _gaq = _gaq || [];
          _gaq.push(['_setAccount', '${trackingCode}']);
          _gaq.push(['_trackPageview']);

          (function() {
            var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
            ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
            var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
          })();

        </script>
        </#if>
        </body>
        </html>
    </#if>
</#macro>

<#macro addThis code>

        <!-- AddThis Button BEGIN -->
        <div class="addthis_toolbox addthis_default_style">
            <a href="http://www.addthis.com/bookmark.php?v=250&amp;username=${addThisTrackingCode}" class="addthis_button_compact">Share</a>
        </div>
        <script type="text/javascript" src="http://s7.addthis.com/js/250/addthis_widget.js#username=${addThisTrackingCode}"></script>
        <!-- AddThis Button END -->
        <script type="text/javascript">
        var addthis_config = {
             ui_language: "no",
            ui_click: true,
            ui_cobrand: "Norvegiana",
            ui_header_color: "#ffffff",
            ui_header_background:"#0071BC"
        }
        </script>
        <br/>

</#macro>

<#macro languageSelect>
<select onchange="setLang(this.options[selectedIndex].value)" name="dd_lang" id="dd_lang">
    <option value="Choose language" selected="selected"><@spring.message '_action.chooselanguage' /></option>
    <option value="en">
    English (eng)
    </option>
    <option value="no">
    Norsk (nor)
    </option>
</select>
  <form method="post" id="frm-lang" name="frm-lang" style="display: none;" action="/${portalName}/">
  <input  type="hidden" name="lang"/>
  </form>

</#macro>

<#macro loginFormModal>

<div id="loginFormModal">
<h2><@spring.message '_mine.login' /></h2>

<form name='f1' id="loginForm" action='j_spring_security_check' method='POST' accept-charset="UTF-8">
<table>
    <tr>
        <td><label for="j_username"><@spring.message '_mine.email.address' /></label></td>
        <td><input type='text' id="j_username" name="j_username" value="" maxlength="50"></td>
    </tr>
    <tr>
        <td><label for="j_password"><@spring.message "_mine.user.register.password" /></label></td>
        <td><input type='password' id="j_password" name='j_password' maxlength="50"/></td>
    </tr>
    <tr>
        <td>
            <a href="/${portalName}/forgot-password.html"><@spring.message '_mine.forgotpassword' /></a>
        </td>
        <td align="right"><input name="submit_login" type="submit" value="<@spring.message '_mine.login' />"/></td>
    </tr>
</table>
<#if errorMessage??>

<strong><@spring.message '_portal.ui.notification.error' />: </strong> Inlog gegevens zijn niet juist

</#if>
</div>
</#macro>
<#--
 * getFacetCount
 *
 * generates a table with 4x4 rows of brief result data
 * @param obj: page object containing the data
 * @param facetName : the name of the facet
 * @param typeName : specific type attribute (e.g. for facetName TYPE, typeName(s) could be IMAGE, TEXT, VIDEO, AUDIO
 -->
<#macro getFacetCount obj facetName typeName>
    <#assign facetMap = obj.getFacetMap()>
    <#assign facet = facetMap.getFacet(facetName)>
    <#assign count = 0/>
    <#if facet.links?size &gt; 0>
        <#list facet.links as link>
            <#if link.value == typeName>
                <#assign count = link.count>
            </#if>
        </#list>
    </#if>
    ${count?trim}
</#macro>

<#--
 * resultGrid
 *
 * generates a table with 4x4 rows of brief result data
 * @param seq : the actual result set
 -->
<#macro resultBriefGrid>
<#assign seq = briefDocs/>
<table summary="gallery view all search results" border="0" class="results zebra">
    <caption>Results</caption>
    <#list seq?chunk(4) as row>
    <tr>
        <#list row as cell>
        <td valign="bottom" class="${cell.type}">
            <div class="brief-thumb-container">
                <a href="${cell.fullDocUrl()}?${queryStringForPresentation}&amp;tab=${tab}&amp;start=${cell.index()?c}&amp;startPage=${pagination.start?c}${defaultParams}&amp;pageId=brd">
                <#--<a href="${cell.fullDocUrl()}?${queryStringForPresentation}&amp;start=${cell.index()?c}&amp;startPage=${pagination.start?c}${defaultParams}&amp;pageId=brd">-->
                    <a href="${cell.fullDocUrl()}?${queryStringForPresentation}&start=${start}&startPage=${pagination.start?c}&pageId=brd&${queryParamList.getDefaultParamsFormatted()}">
                    <#if useCache="true">
                         <img
                                 class="thumb"
                                 id="thumb_${cell.index()?c}"
                                 align="middle"
                                 src="${cacheUrl}id=${cell.thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${cell.type}" alt="<@spring.message '_action.alt.more.info' />"
                                 onload="checkSize(this.id,'brief',this.width);"
                                 onerror="showDefaultSmall(this,'${cell.type}')"
                                 height="110"
                          />
                    <#else>
                        <img
                                class="thumb"
                                id="thumb_${cell.index()?c}"
                                align="middle"
                                src="${cell.thumbnail}"
                                alt="Click for more information"
                                height="110"
                                onload="checkSize(this.id,'brief',this.width);"
                                onerror="showDefaultSmall(this,'${cell.type}')"
                         />
                    </#if>
                </a>
            </div>
            <div class="brief-content-container">
            <h6>
                <#--<a href="${cell.fullDocUrl()}?${queryStringForPresentation}&amp;tab=${tab}&amp;start=${cell.index()?c}&amp;startPage=${pagination.start?c}&amp;uri=${cell.id}${defaultParams}&amp;pageId=brd">-->
                <#--<a href="${cell.fullDocUrl()}?${queryStringForPresentation}&amp;start=${cell.index()?c}&amp;startPage=${pagination.start?c}&amp;uri=${cell.id}${defaultParams}&amp;pageId=brd">-->
                 <a href="${cell.fullDocUrl()}?${queryStringForPresentation}&start=${start}&startPage=${pagination.start?c}&pageId=brd&${queryParamList.getDefaultParamsFormatted()}">
                    <@stringLimiter "${cell.title}" "40"/>
                </a>
            </h6>
            <ul>
                <#if cell.creator??>
                    <#if !(cell.creator = " " || cell.creator = "," || cell.creator = "Unknown,")>
                        <li><@stringLimiter "${cell.creator}" "120"/></li>
                    </#if>

                </#if>
                <#if cell.year != "">
                    <#if cell.year != "0000">
                        <li>${cell.year}</li>
                    </#if>

                </#if>
                <#if cell.dataProvider != "">
                    <#assign pr = cell.dataProvider />
                    <#if pr?length &gt; 80>
                        <#assign pr = cell.dataProvider?substring(0, 80) + "..."/>
                    </#if>
                    <li title="${cell.dataProvider}"><span class="provider">${pr}</span></li>
                </#if>
            </ul>
            </div>
        </td>
        </#list>
    </tr>
    </#list>
</table>
</#macro>

<#--
 * resultList
 *
 * generates a list of brief result data
 * @param seq : the actual result set
 -->
<#macro resultBriefList>
<#assign seq = briefDocs/>
<table cellspacing="1" cellpadding="0" width="100%" border="0" summary="search results" class="results list zebra">
    <#list seq as cell>
    <tr>
        <td valign="top" width="80">
            <div class="brief-thumb-container-listview">
                <#--<a href="${cell.fullDocUrl()}?${queryStringForPresentation}&amp;start=${cell.index()?c}&amp;startPage=${pagination.start?c}${defaultParams}&amp;pageId=brd">-->
                <a href="${cell.fullDocUrl()}?${queryStringForPresentation}&start=${start}&startPage=${pagination.start?c}&pageId=brd&${queryParamList.getDefaultParamsFormatted()}">
                    <#if useCache="true">
                        <img class="thumb"
                             id="thumb_${cell.index()}"
                             align="middle"
                             src="${cacheUrl}id=${cell.thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${cell.type}"
                             alt="<@spring.message '_action.alt.more.info' />"
                             height="50"
                          />
                    <#else>
                        <img class="thumb"
                             id="thumb_${cell.index()?c}"
                             align="middle"
                             src="${cell.thumbnail}"
                             alt="Click for more information"
                             height="50"
                             onerror="showDefaultSmall(this,'${cell.type}')"
                         />
                    </#if>
                </a>
            </div>
        </td>
        <td class="${cell.type} ">
                <h6>
                  <#--<a class="fg-gray" href="${cell.fullDocUrl()}?${queryStringForPresentation}&amp;start=${cell.index()?c}&amp;startPage=${pagination.start?c}${defaultParams}&amp;pageId=brd">-->
                  <a class="fg-gray" href="${cell.fullDocUrl()}?${queryStringForPresentation}&start=${start}&startPage=${pagination.start?c}&pageId=brd&${queryParamList.getDefaultParamsFormatted()}">
                        <@stringLimiter "${cell.title}" "100"/></a>
                </h6>
                <p>
                <#if !cell.creator?matches("")><span><@spring.message '_search.field.creator' />: </span>${cell.creator}<br/></#if>
                <#if !cell.year?matches("")><#if cell.year != "0000"><span><@spring.message '_search.field.date' />: </span>${cell.year}<br/></#if></#if>
                <#if !cell.dataProvider?matches("")><@spring.message '_search.field.provider' />: <span class="provider">${cell.dataProvider}</span><br></#if>
                <#if !cell.type?matches("")><@spring.message '_metadata.dc.type' />: <span class="type">${cell.type}</span></#if>
                </p>
        </td>
    </tr>
    </#list>
</table>
</#macro>


<#--
 * resultFacets
 *
 * Macro to generate lists of result facets and their links
 -->

<#macro resultBriefFacets key facetLanguageTag columnSize>
    <#assign facetMap = result.getFacetMap()>
    <#assign facet = facetMap.getFacet(key)>
    <#if !facet.type?starts_with("unknown")>
        <h4 class="trigger <#if facet.selected>active</#if>"><a href="#"><@spring.message '${facetLanguageTag}' /></a></h4>
        <div class="facets_container">
        <#if facet.links?size &gt; 0>
            <table summary="A list of facets to help refine your search">
                <#list facet.links?chunk(columnSize?int) as row>
                    <tr>
                        <#list row as link>
                            <td align="left" style="padding: 2px;" <#if (columnSize==2)>width="50%"</#if>>
                            <#-- DO NOT ENCODE link.url. This is already done in the java code. Encoding it will break functionality !!!  -->
                                <#if !link.remove = true>
                                    <a class="add" href="?query=${query?html}${link.url?html}&amp;${defaultParams}" title="${link.value}">
                                    <#--<input type="checkbox" value="" onclick="document.location.href='?query=${query?html}${link.url}';"/>-->
                                        <@stringLimiter "${link.value}" "25"/><span>(${link.count})</span>
                                    </a>
                                <#else>
                                    <a class="remove" href="?query=${query?html}${link.url?html}&amp;${defaultParams}" title="${link.value}">
                                        <@stringLimiter "${link.value}" "25"/>(<span>${link.count})</span>
                                    </a>
                                </#if>
                            </td>
                        </#list>
                    </tr>
                </#list>
            </table>
        </#if>
        </div>
    </#if>
</#macro>
<#--
 * resultPagination
 *
 * generates pagination links from brief results views
 -->
<#macro resultBriefPagination>
<#assign pagination = pagination/>
    <#list pagination.pageLinks as link>
        <#if link.linked>
            <#assign lstart = link.start/>
                <#--<a href="?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${link.start?c}${defaultParams}">-->
                <a href="?${queryStringForPresentation?html}&amp;start=${link.start?c}&amp;${queryParamList.getDefaultParamsFormatted()}">
                    ${link.display?c}
                </a>
         <#else>
            <a>
                <strong>${link.display?c}</strong>
            </a>
        </#if>
    </#list>
    <#if pagination.previous>
        <#--<a href="?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${pagination.previousPage?c}${defaultParams}" alt="<@spring.message '_action.alt.previous.page' />">-->
        <a href="?${queryStringForPresentation?html}}&amp;start=${pagination.previousPage?c}&amp;${queryParamList.getDefaultParamsFormatted()}" alt="<@spring.message '_action.alt.previous.page' />">
       <@spring.message '_action.previous' />
    </a>
    </#if>
    <#if pagination.next>
        <#--<a href="?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${pagination.nextPage?c}${defaultParams}" alt="<@spring.message '_action.alt.next.page' />">-->
        <a href="?${queryStringForPresentation?html}&amp;start=${pagination.nextPage?c}&amp;${queryParamList.getDefaultParamsFormatted()}" alt="<@spring.message '_action.alt.next.page' />">
            <@spring.message '_portal.ui.navigation.next' />
        </a>
    </#if>
</#macro>

<#macro resultBriefPaginationStyled>
        <div class="fg-buttonset fg-buttonset-multi">

            <#list pagination.pageLinks as link>
            <#assign uiClassBorder = ""/>
            <#if link_index == 0>
                <#assign uiClassBorder = "ui-corner-left"/>
            </#if>
            <#if !link_has_next>
                <#assign uiClassBorder = "ui-corner-right"/>
            </#if>
                <#if link.linked>
                    <#assign lstart = link.start/>
                        <a
                                href="?${queryStringForPresentation?html}&amp;start=${link.start?c}&amp;${queryParamList.getDefaultParamsFormatted()}"
                                class="fg-button ui-state-default ${uiClassBorder}"
                        >
                            ${link.display?c}
                        </a>
                 <#else>
                    <a class="fg-button ui-state-default ui-state-active ${uiClassBorder}">
                        <strong>${link.display?c}</strong>
                    </a>
                </#if>
            </#list>

            <#assign uiClassStatePrev = ""/>
            <#assign uiClassStateNext = ""/>
            <#if !pagination.previous>
                <#assign uiClassStatePrev = "ui-state-disabled">
            </#if>
            <#if !pagination.next>
                <#assign uiClassStateNext = "ui-state-disabled">
            </#if>
            <a
                    href="?${queryStringForPresentation?html}&amp;start=${pagination.previousPage?c}&amp;${queryParamList.getDefaultParamsFormatted()}"
                    class="fg-button ui-state-default fg-button-icon-left ui-corner-all ${uiClassStatePrev}"
                    alt="<@spring.message '_action.alt.previous.page' />"
                    style="margin: 0 8px;"
                    >
               <span class="ui-icon ui-icon-circle-arrow-w"></span><@spring.message '_action.previous' />
            </a>
            <a
                    href="?${queryStringForPresentation?html}&amp;start=${pagination.nextPage?c}&amp;${queryParamList.getDefaultParamsFormatted()}"
                    class="fg-button ui-state-default fg-button-icon-right ui-corner-all ${uiClassStateNext}"
                    alt="<@spring.message '_action.alt.next.page' />"
                    >
                    <span class="ui-icon ui-icon-circle-arrow-e"></span><@spring.message '_portal.ui.navigation.next' />
            </a>
        </div>
</#macro>

<#--
 * resultPaginationList
 *
 * generates an unordered list of pagination links from brief results views
 -->
<#macro resultBriefPaginationList>
<#assign pagination = pagination/>
<ul>
    <#list pagination.pageLinks as link>
        <li>
            <#if link.linked>
                <#assign lstart = link.start/>
                <#--<a href="?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${link.start?c}${defaultParams}">-->
                <a href="?${queryStringForPresentation?html}&amp;start=${link.start?c}${defaultParams}">
                ${link.display?c}
                </a>
                <#else>
                    <strong>${link.display?c}</strong>
            </#if>
        </li>
    </#list>
    <#if pagination.previous>
    <li>
        <a
            <#--href="?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${pagination.previousPage?c}${defaultParams}"-->
            href="?${queryStringForPresentation?html}&amp;start=${pagination.previousPage?c}${defaultParams}"
            alt="<@spring.message '_action.alt.previous.page' />"
        >
        <@spring.message '_action.previous' />
        </a>
    </li>
    </#if>
    <#if pagination.next>
    <li>
        <a
            <#--href="?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${pagination.nextPage?c}${defaultParams}"-->
            href="?${queryStringForPresentation?html}&amp;start=${pagination.nextPage?c}${defaultParams}"
            alt="<@spring.message '_action.alt.next.page' />"
        >
        <@spring.message '_portal.ui.navigation.next' />
        </a>
    </li>
    </#if>
</ul>
</#macro>

<#--
 * resultQueryBreadcrumbs
 *
 * Macro to generate a query result breadcrumbs
 -->
<#macro resultBriefQueryBreadcrumbs>
<#assign breadcrumbs = breadcrumbs/>
    <@spring.message '_portal.ui.navigation.matchesfor' />:
        <#if !result.matchDoc??>
            <#list breadcrumbs as crumb>
                <#if !crumb.last>
                    <a href="?${crumb.href}">${crumb.display?html}</a>
                <#else>
                    <strong>${crumb.display?html}</strong>
                </#if>
            </#list>
        <#else>
            <@spring.message '_portal.ui.viewingrelateditems' />
            <#assign match = result.matchDoc/>
            <a href="${match.fullDocUrl()}">
                <#if useCache="true">
                    <img src="${cacheUrl}id=${match.thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${match.type}" alt="${match.title}" height="25"/>
                <#else>
                    <img src="${match.thumbnail}" alt="${match.title}" height="25"/>${match.title}
                </#if>
            </a>
        </#if>
</#macro>

<#--
 * resultQueryBreadcrumbsList
 *
 * Macro to generate a definition list with query result breadcrumbs
 -->
<#macro resultBriefQueryBreadcrumbsList>
<#assign breadcrumbs = breadcrumbs/>
    <dl class="breadcrumbs">
        <dt><@spring.message '_portal.ui.navigation.matchesfor' />:</dt>
        <#if !result.matchDoc??>
            <#list breadcrumbs as crumb>
                <#if !crumb.last>
                    <dd <#if crumb_index == 0>class="first"</#if>><a href="?${crumb.href}">${crumb.display?html}</a></dd>
                <#else>
                    <dd <#if crumb_index == 0>class="first"</#if>><strong>${crumb.display?html}</strong></dd>
                </#if>
            </#list>
        <#else>
            <dd>
                <@spring.message '_portal.ui.viewingrelateditems' />
                <#assign match = result.matchDoc/>
                <a href="${match.fullDocUrl}">
                    <#if useCache="true">
                        <img src="${cacheUrl}id=${match.thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${match.type}" alt="${match.title}" height="25"/>
                    <#else>
                        <img src="${match.thumbnail}" alt="${match.title}" height="25"/>
                    </#if>
                </a>
            </dd>
        </#if>
    </dl>
</#macro>

<#macro resultsBriefUserActions>
<#assign seq = briefDocs/>
    <#if seq?size &gt; 0>
        <dl class="menu">
            <dt>
               <@spring.message '_portal.ui.message.actions'/>
            </dt>
            <dd>
                <#if user??>
                    <a id="saveQuery" href="#" onclick="saveQuery('SavedSearch', '${queryToSave?url("utf-8")?js_string}', '${query?url("utf-8")?js_string}');"><@spring.message '_action.save.this.search'/></a>
                <#else>
                    <a href="#" onclick="highLight('a#login, a#register'); showMessage('error','<@spring.message '_mine.user.notification.login.required'/>'); return false" class="disabled"><@spring.message '_action.save.this.search'/></a>
                </#if>
            </dd>
        </dl>
        <div id="msg-save-search" class="msg-hide"></div>
    </#if>
</#macro>

<#macro resultsFullQueryBreadcrumbs>
    <#if pagination??>
        <@spring.message '_portal.ui.navigation.matchesfor' />:
            <#if !query?starts_with("europeana_uri:")>
                <#list pagination.breadcrumbs as crumb>
                    <#if !crumb.last>
                        <a href="${portalName}/brief-doc.html?${crumb.href}">${crumb.display?html}</a>&#160;>&#160;
                    <#else>
                        <strong>${crumb.display?html}</strong>
                    </#if>
                </#list>
            <#-- TODO: find a more elegant way of indicating to which item the related items being viewed belong to -->
            <#--<#else>-->
                    <#--<@spring.message '_portal.ui.viewingrelateditems' />-->
                    <#--<#assign match = result.fullDoc />-->
                    <#--<#assign imgSrc = match.getAsString("europeana_object")/>-->
                    <#--<#assign type = match.getFieldValue("europeana_type").getFirst()/>-->
                    <#--<#assign title = match.getFieldValue("dc_title").getFirst()/>-->
                    <#--&lt;#&ndash;todo review this. It seems wrong to display the image of the current full-doc instead of the original related item search&ndash;&gt;-->
                    <#--<a href="full-doc.html?&amp;uri=${match.id}">-->
                    <#--<#if useCache="true">-->
                        <#--&lt;#&ndash;<img src="${cacheUrl}id=${match.thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${match.type}" alt="${match.title}" height="25"/>&ndash;&gt;-->
                        <#--<img src="${cacheUrl}id=${imgSrc?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${type}" alt="${title}" height="25"/>-->
                    <#--<#else>-->
                        <#--<img src="${imgSrc}" alt="${match.getFieldValue("dc_title").getFirst()}" height="25"/>-->
                    <#--</#if>-->
                    <#--</a>-->
            </#if>
    </#if>
</#macro>

<#macro resultsFullQueryBreadcrumbsList>
    <#if pagination??>
        <dl>
            <dt><@spring.message '_portal.ui.navigation.matchesfor' />:</dt>
            <#if !query?starts_with("europeana_uri:")>
                <#list pagination.breadcrumbs as crumb>
                    <#if !crumb.last>
                        <dd <#if crumb_index == 0>class="nobg"</#if>><a href="?${crumb.href}">${crumb.display?html}</a>&#160;>&#160;</dd>
                    <#else>
                        <dd <#if crumb_index == 0>class="nobg"</#if>><strong>${crumb.display?html}</strong></dd>
                    </#if>
                </#list>
            <#else>
                <dd class="nobg">
                    <@spring.message '_portal.ui.viewingrelateditems' />
                    <#assign match = result.fullDoc />
                    <#--todo review this. It seems wrong to display the image of the current full-doc instead of the original related item search-->
                    <a href="full-doc.html?&amp;uri=${match.id}">
                    <#if useCache="true">
                        <img src="${cacheUrl}id=${match.thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${match.type}" alt="${match.title}" height="25"/>
                    <#else>
                        <img src="${match.thumbnail}" alt="${match.title}" height="25"/>
                    </#if>
                    </a>
                </dd>
            </#if>
        </dl>
    <#else>
        <ul>
            <li>&#160;</li>
        </ul>
    </#if>
</#macro>

<#macro resultFullPagination>

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

        <a href="${urlPrevious}" class="fg-button ui-state-default fg-button-icon-left ui-corner-all ${uiClassStatePrev}" alt="<@spring.message '_action.alt.previous.page' />">
            <span class="ui-icon ui-icon-circle-arrow-w"></span><@spring.message '_action.previous' />
        </a>

        <a href="${urlNext}" class="fg-button ui-state-default fg-button-icon-right ui-corner-all ${uiClassStateNext}" alt="<@spring.message '_action.alt.next.page' />">
            <span class="ui-icon ui-icon-circle-arrow-e"></span><@spring.message '_portal.ui.navigation.next' />
        </a>

        <#if pagination.returnToResults??>
            <a class="fg-button ui-state-default fg-button-icon-left ui-corner-all" href="${pagination.returnToResults?html}" alt="<@spring.message '_action.return.to.results' />">
               <span class="ui-icon ui-icon-circle-arrow-n"></span><@spring.message '_action.return.to.results' />
            </a>
        </#if>


    </#if>

</#macro>


<#macro resultFullImage>

    <#assign thumbnail = result.fullDoc.getFieldValue("europeana_object")/>
    <#if !thumbnail.isNotEmpty()>
        <#assign thumbnail = "noImageFound"/>
    <#else>
        <#assign thumbnail = thumbnail.getFirst()/>
    </#if>
    <#--<#assign imageRef = "#"/>-->
    <#--<#assign isShownBy = result.fullDoc.getFieldValue("europeana_isShownBy")/>-->
    <#--<#assign isShownAt = result.fullDoc.getFieldValue("europeana_isShownAt")/>-->

    <#--<#if isShownBy.isNotEmpty()>-->
        <#--<#assign imageRef = isShownBy.getFirst()/>-->
    <#--<#elseif isShownAt.isNotEmpty()>-->
        <#--<#assign imageRef = isShownAt.getFirst()/>-->
    <#--</#if>-->

    <#assign overlayActive = false/>
    <#assign overlayUrl = result.fullDoc.getFieldValue("europeana_isShownBy").getFirst()/>
    <#assign originalContextUrl = result.fullDoc.getFieldValue("europeana_isShownAt").getFirst()/>

    <#if !overlayUrl?matches(" ")>
        <#assign overlayUrl = overlayUrl/>
        <#assign overlayActive = true/>
    </#if>

    <#if !originalContextUrl?matches(" ") && !overlayUrl?matches(" ")>
        <#assign originalContextUrl = originalContextUrl/>
    <#elseif originalContextUrl?matches(" ") && !overlayUrl?matches(" ")>
        <#assign originalContextUrl = overlayUrl/>
    <#elseif !originalContextUrl?matches(" ") && overlayUrl?matches(" ")>
        <#assign overlayUrl = originalContextUrl/>
        <#assign overlayActive = false/>
    </#if>


    <#--overlayURL: ${overlayUrl}<br/>-->
    <#--originalContextUrl: ${originalContextUrl}-->
   <#--<a href="/${portalName}/redirect.html?shownBy=${isShownAt.getFirst()}&provider=${result.fullDoc.europeanaProvider[0]}&id=${result.fullDoc.id}"-->
      <#--target="_blank"-->
      <#--<#if overlayActive = true>-->
      <#--class="overlay"-->
      <#--</#if>-->
    <#-->-->
    <#--<a class="<#if overlayActive>overlay</#if>"-->
       <#--href="/${portalName}/redirect.html?shownBy=${overlayUrl?url('utf-8')}&provider=${result.fullDoc.europeanaProvider[0]}&id=${result.fullDoc.id}"-->
       <#--target="_blank"-->
       <#--alt="<@spring.message '_action.view.in.original.context' /> <@spring.message '_action.OpenInNewWindow'/>"-->
    <#-->-->
    <#if useCache="true">
        <img src="${cacheUrl}id=${thumbnail?url('utf-8')}&amp;size=FULL_DOC&amp;type=${result.fullDoc.europeanaType}"
             class="full"
             alt="${result.fullDoc.getAsString("dc_title")}"
             id="imgview"
             onload="checkSize(this.id,'full',this.width);"
             onerror="showDefaultLarge(this,'${result.fullDoc.europeanaType}',this.src)"
        />
    <#else>
        <img
            alt="${result.fullDoc.getAsString("dc_title")}
            id="imgview"
            src="${thumbnail}"
            onload="checkSize(this.id,'full',this.width);"
            onerror="showDefaultLarge(this,'${result.fullDoc.europeanaType}',this.src)"
            alt="<@spring.message '_action.view.in.original.context' /> <@spring.message '_action.OpenInNewWindow'/>"
        />
    </#if>
    <#--<#if useCache="true">-->
        <#--<img src="${cacheUrl}id=${thumbnail?url('utf-8')}&amp;size=FULL_DOC&amp;type=${result.fullDoc.europeanaType}"-->
             <#--class="full"-->
             <#--alt="${result.fullDoc.dcTitle[0]}"-->
             <#--id="imgview"-->
             <#--onload="checkSize(this.id,'full',this.width);"-->
             <#--onerror="showDefaultLarge(this,'${result.fullDoc.europeanaType}',this.src)"-->
         <#--/>-->
    <#--<#else>-->
        <#--<img-->
             <#--alt="${result.fullDoc.dcTitle[0]}"-->
             <#--id="imgview"-->
             <#--class="full"-->
             <#--src="${thumbnail}"-->
             <#--onload="checkSize(this.id,'full',this.width);"-->
             <#--onerror="showDefaultLarge(this,'${result.fullDoc.europeanaType}',this.src)"-->
         <#--/>-->
    <#--</#if>-->

    <#--</a>-->
    <#-- originalContextUrl assigned top of page -->
    <#if !originalContextUrl?matches(" ")>
    <nav style="padding: 1em;">
    <a
            href="/${portalName}/redirect.html?shownAt=${originalContextUrl?url('utf-8')}&provider=${result.fullDoc.europeanaProvider[0]}&id=${result.fullDoc.id}"
            target="_blank"
            alt="<@spring.message '_action.view.in.original.context' /> - <@spring.message '_action.OpenInNewWindow'/>"
            title="<@spring.message '_action.view.in.original.context' /> - <@spring.message '_action.OpenInNewWindow'/>"
            class="fg-button ui-state-default fg-button-icon-left ui-corner-all"
            style="float: none;;"
            >
        <span class="ui-icon ui-icon-newwin"></span><@spring.message '_action.view.in.original.context' />
    </a>
    </nav>

    </#if>
</#macro>

<#macro resultFullListCustom>
    <table summary="This table contains the metadata for the object being viewed" width="100%">
        <caption>Object metadata</caption>
        <tbody>
            <@resultFullDataRow "dc_title"/>
            <@resultFullDataRow "dc_date"/>
            <@resultFullDataRow "dc_creator"/>
            <@resultFullDataRow "dc_description"/>
            <@resultFullDataRow "dc:subject"/>
            <@resultFullDataRow "dc_language"/>
            <@resultFullDataRow "dc_source"/>
            <@resultFullDataRow "dc_rights"/>
            <@resultFullDataRow "europeana_dataProvider"/>
            <@resultFullDataRow "europeana_country"/>
            <@resultFullDataRow "dc_identifier"/>
            <@resultFullDataRow "dc_publisher"/>
            <@resultFullDataRow "dc_coverage"/>
            <@resultFullDataRow "europeana_type"/>
            <@resultFullDataRow "dc_relation"/>

        </tbody>
    </table>
</#macro>

<#macro resultFullList>
    <table summary="This table contains the metadata for the object being viewed" width="100%">
        <caption>Object metadata</caption>
        <tbody>

        <#list result.fullDoc.getFieldValuesFiltered(false, ['europeana_uri', 'delving_pmhId', 'europeana_collectionName', 'europeana_collectionTitle',
        'europeana_object', 'europeana_isShownAt', 'europeana_isShownBy', 'europeana_language', 'europeana_rights', 'europeana_type']) as field>
            <tr>
                <#--<th scrope="row">${field.getKeyAsXml()}</th>-->
                <th scope="row"><@spring.messageText '${field.getKeyAsMessageKey()}', '${field.getKeyAsXml()}' />:</th>
                <td>${field.getFirst()}</td>
            </tr>
        </#list>
        </tbody>
    </table>
</#macro>

<#macro resultFullListFormatted>
    <table summary="This table contains the metadata for the object being viewed" width="100%" class="lined">
        <caption>Object metadata</caption>
        <#assign doc = result.fullDoc/>
        <tbody>
            <@resultFullDataRowConcatenated doc.getConcatenatedArray("dc_title", ["dc_title"])/>
            <@resultFullDataRowConcatenated doc.getConcatenatedArray("dc_creator", ["dc_creator"])/>
            <@resultFullDataRowConcatenated doc.getConcatenatedArray("searchfield.contributor", ["dc_contributor"])/>
            <@resultFullDataRowConcatenated doc.getConcatenatedArray("dc_date", ["dc_date", "dcterms_created", "dcterms_issued"])/>
            <@resultFullDataRowConcatenated doc.getConcatenatedArray("dc_type", ["dc_type"])/>
            <@resultFullDataRowConcatenated doc.getConcatenatedArray("dc_format", ["dc_format", "dcterms_extent", "dcterms_medium"])/>
            <@resultFullDataRowConcatenated doc.getConcatenatedArray("dc_language", ["dc_language"])/>
            <@resultFullDataRowConcatenated doc.getConcatenatedArray("dc_description", ["dc_description"])/> <#-- todo add return + trunc -->
            <@resultFullDataRowConcatenated doc.getConcatenatedArray("dc_subject", ["dc_subject", "dcterms_temporal", "dcterms_spatial", "dc_coverage"])/>
            <@resultFullDataRowConcatenated doc.getConcatenatedArray("dc_identifier", ["dc_identifier"])/>
            <@resultFullDataRowConcatenated doc.getConcatenatedArray("dc_source", ["dc_source"])/>
            <@resultFullDataRowConcatenated doc.getConcatenatedArray("dc_rights", ["dc_rights"])/>
            <@resultFullDataRowConcatenated doc.getConcatenatedArray("dc_publisher", ["dc_publisher"])/>
            <@resultFullDataRowConcatenated2 doc.getConcatenatedArray("europeana_provider", ["europeana_provider", "europeana_dataProvider"]) doc.getFieldValue("europeana_country")/>
            <@resultFullDataRowConcatenated doc.getConcatenatedArray("dc_relation", ["dc_relation", "dcterms_references",
            "dcterms_isReferencedBy", "dcterms_isReplacedBy", "dcterms_isRequiredBy", "dcterms_isPartOf", "dcterms_hasPart",
            "dcterms_replaces", "dcterms_requires", "dcterms_isVersionOf", "dcterms_hasVersion",
            "dcterms_conformsTo", "dcterms_hasFormat"])/>

        </tbody>
    </table>
</#macro>


<#macro resultFullDataRowConcatenated fieldFormatted>
    <#if fieldFormatted.isNotEmpty()>
        <tr>
            <th scope="row"><@spring.messageText '${fieldFormatted.getKeyAsMessageKey()}', '${fieldFormatted.getKey()}' />:</th>
            <td>${fieldFormatted.getValuesFormatted(";&#160;")}
            </td>
        </tr>
    </#if>
</#macro>

<#macro resultFullDataRowConcatenated2 fieldFormatted fieldvalue>
    <#if fieldFormatted.isNotEmpty()>
        <tr>
            <th scope="row"><@spring.messageText '${fieldFormatted.getKeyAsMessageKey()}', '${fieldFormatted.getKey()}' />:</th>
            <td>${fieldFormatted.getValuesFormatted(";&#160;")}<#if fieldvalue.isNotEmpty()>;&#160; ${fieldvalue.getFirst()}</#if></td>
        </tr>
    </#if>
</#macro>

<#macro resultFullDataRow key>
    <#assign keyVal = result.fullDoc.getFieldValue(key)/>
    <#if keyVal.isNotEmpty()>
        <tr>
            <th scope="row"><@spring.messageText '${keyVal.getKeyAsMessageKey()}', '${keyVal.getKeyAsXml()}' />:</th>
            <td>${keyVal.getArrayAsString(";&#160;")}</td>
        </tr>
    </#if>
</#macro>

<#--
 * simpleSearch
 *
 * Macro to generate a simple search form.
 -->
<#macro simpleSearch>
<form method="get" action="/${portalName}/brief-doc.html" accept-charset="UTF-8" id="formSimpleSearch">
    <input type="hidden" name="start" value="1" />
    <input type="hidden" name="view" value="${view}" />
    <fieldset>
        <legend>Search</legend>
        <#--<input name="query" id="query" type="text" title="Europeana Search" maxlength="100" />-->
        <input name="query" id="query" type="query" title="Search" maxlength="100" autofocus="true" class="ui-corner-all required" />
        <#--<button id="submitSimpleSearch" type="submit" class="btn-strong"><@spring.message '_action.search' /></button>-->
        <input type="submit" id="submitSimpleSearch" class="btn-strong" value="<@spring.message '_action.search' />"/>
        <nav>
        <a href="/${portalName}/advancedsearch.html" title="<@spring.message '_action.advanced.search' />"><@spring.message '_action.advanced.search' /></a>
        </nav>
    </fieldset>
</form>
</#macro>

<#--
 * stringLimiter
 *
 * Macro which takes two parameters:
 * @param theStr : the string to be shortened
 * @param size : the desired length of the string.
 * It returns the shortened string with elipses at the end
 -->
<#macro stringLimiter theStr size>
    <#assign newStr = theStr>
    <#if newStr?length &gt; size?number>
    <#assign newStr = theStr?substring(0,size?number) + "...">
    </#if>
    ${newStr}
</#macro>

<#--
 * userBar
 *
 * Macro to generate a list of links to login / Register and user-saved items.
 -->
<#macro userBar>
<ul>
   <li><a id="home" href="/${portalName}/">Home<#-- todo add message tag --></a></li>
    <#if !user??>
        <li><a id="login" href="/${portalName}/login.html"><@spring.message '_mine.login'/></a></li>
        <li><a id="register" href="/${portalName}/register-request.html"><@spring.message '_mine.user.register.register'/></a></li>
    </#if>
    <#if user??>
    <li>
        <@spring.message '_mine.loggedinas' />: <strong>${user.userName?html}</strong> |
            <a id="logout" href="/${portalName}/logout.html"><@spring.message '_mine.logout' /></a>
    </li>
    <#if user.savedItems??>
    <li>
        <a href="/${portalName}/mine.html" onclick="$.cookie('ui-tabs-3', '1', { expires: 1 });">
            <@spring.message '_mine.saved.items' />
        </a>
        (<span id="savedItemsCount">${user.savedItems?size}</span>)
    </li>
    </#if>
    <#if user.savedSearches??>
    <li>
        <a href="/${portalName}/mine.html" onclick="$.cookie('ui-tabs-3', '2', { expires: 1 });">
            <@spring.message '_mine.saved.searches' />
        </a>
        (<span id="savedSearchesCount">${user.savedSearches?size}</span>)
    </li>
    </#if>
    </#if>
</ul>
<#--<div id="overlayContainer"></div>-->
</#macro>

<#--
 * viewSelect
 *
 * Macro to generate options to select the grid or list view
 -->
<#macro viewSelect>
<div id="viewselect">
    <#if queryStringForPresentation?exists>
        <#assign viewChangeUrl>${queryParamList.getListFilteredFormatted(false,['view'])?trim}</#assign>
        <#--<#if view="table">-->
            <#--<a href="?${queryStringForPresentation?html}&amp;view=table&amp;${defaultParams}" title="<@spring.message '_action.alt.table.view' />">&nbsp;<img src="/${portalName}/${portalTheme}/images/btn-multiview-hi.gif" alt="<@spring.message '_action.alt.table.view' />" /></a>-->
            <#--<a href="?${queryStringForPresentation?html}&amp;view=list&amp;${defaultParams}" title="<@spring.message '_action.alt.list.view' />" >&nbsp;<img src="/${portalName}/${portalTheme}/images/btn-listview-lo.gif" alt="<@spring.message '_action.alt.list.view' />" /></a>-->
        <#--<#else>-->
            <#--<a href="?${queryStringForPresentation?html}&amp;view=table&amp;${defaultParams}" title="<@spring.message '_action.alt.table.view' />">&nbsp;<img src="/${portalName}/${portalTheme}/images/btn-multiview-lo.gif" alt="<@spring.message '_action.alt.table.view' />" hspace="5"/></a>-->
            <#--<a href="?${queryStringForPresentation?html}&amp;view=list&amp;${defaultParams}" title="<@spring.message '_action.alt.list.view' />">&nbsp;<img src="/${portalName}/${portalTheme}/images/btn-listview-hi.gif" alt="<@spring.message '_action.alt.list.view' />" hspace="5"/></a>-->
        <#--</#if>-->
        <#if view="table">
            <a href="?${viewChangeUrl}&view-table" title="<@spring.message '_action.alt.table.view' />">&nbsp;<img src="/${portalName}/${portalTheme}/images/btn-multiview-hi.gif" alt="<@spring.message '_action.alt.table.view' />" /></a>
            <a href="?${viewChangeUrl}&view=list&amp;${defaultParams}" title="<@spring.message '_action.alt.list.view' />" >&nbsp;<img src="/${portalName}/${portalTheme}/images/btn-listview-lo.gif" alt="<@spring.message '_action.alt.list.view' />" /></a>
        <#else>
            <a href="?${viewChangeUrl}&view=table" title="<@spring.message '_action.alt.table.view' />">&nbsp;<img src="/${portalName}/${portalTheme}/images/btn-multiview-lo.gif" alt="<@spring.message '_action.alt.table.view' />" hspace="5"/></a>
            <a href="?${viewChangeUrl}&view=list&amp;${defaultParams}" title="<@spring.message '_action.alt.list.view' />">&nbsp;<img src="/${portalName}/${portalTheme}/images/btn-listview-hi.gif" alt="<@spring.message '_action.alt.list.view' />" hspace="5"/></a>
        </#if>
    </#if>
</div>
</#macro>

<#--
 * sortResults
 *
 * Macro to generate a dropdown with sorting options
 -->
<#macro sortResults>
<select id="sortOptions" name="sortBy" onchange="$('input#sortBy').val(this.value);$('form#form-sort').submit();">
    <option value=""><@spring.message '_action.search.order.by' /></option>
    <option value="title" ><@spring.message '_metadata.dc.title' /></option>
    <option value="creator"><@spring.message '_metadata.dc.creator' /></option>
    <option value="YEAR"><@spring.message '_metadata.dc.date' /></option>
    <option value="${ramdomSortKey}">Random</option>
<#--<option value="COLLECTION"><@spring.message '_search.field.collection' /></option>-->
</select>
<form action="" method="GET" id="form-sort" style="display:none;">
    <input type="hidden" name="query" value="${justTheQuery}"/>
    <#-- commented out 'start': re-ordering result should start at p.1 -->
    <#--<input type="hidden" name="start" value="${start}"/>-->
    <input type="hidden" name="view" value="${view}"/>
    <input type="hidden" name="tab" value="${tab}"/>
    <input type="hidden" name="sortBy" id="sortBy" value=""/>
</form>
</#macro>

