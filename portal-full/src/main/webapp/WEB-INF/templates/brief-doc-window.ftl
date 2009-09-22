<#import "spring.ftl" as spring />
<#assign thisPage = "brief-doc.html"/>
<#assign cacheUrl = cacheUrl/>
<#assign queryStringForPresentation = queryStringForPresentation/>
<#assign queryToSave = queryToSave />
<#assign view = "table"/>
<#if format??> <#assign view = format></#if>
<#if RequestParameters.view??> <#assign view = "${RequestParameters.view}"/></#if>
<#if RequestParameters.query??> <#assign query = "${RequestParameters.query?url('utf-8')}"/></#if>
<#if result??><#assign result = result/></#if>
<#assign allCount = 0 />
<#assign textCount = 0 />
<#assign imageCount = 0 />
<#assign videoCount = 0 />
<#assign audioCount = 0 />
<#assign showLanguage = 0 />
<#assign showYear = 0 />
<#assign showType = 0 />
<#assign showProvider = 0 />
<#assign showCountry = 0 />
<#assign showUserTags = 0 />
<#assign next = nextQueryFacets>
<#compress>
<#list result.facets as facet>
    <#if facet.type="TYPE">
        <#list facet.counts as type>
            <#if type.value?lower_case = "image">
                <#assign imageCount = type.count />
            </#if>
            <#if type.value?lower_case = "text">
                <#assign textCount = type.count />
            </#if>
            <#if type.value?lower_case = "video">
                <#assign videoCount = type.count />
            </#if>
            <#if type.value?lower_case = "sound">
                <#assign audioCount = type.count />
            </#if>
        </#list>
    </#if>
</#list>
<#list next as facet>
    <#if facet.type="TYPE">
        <#list facet.links as type>
            <#if type.value?upper_case = "IMAGE">
                <#assign IMAGEUrl = type.url?replace("&qf=TYPE:VIDEO","")?replace("&qf=TYPE:TEXT", "")?replace("&qf=TYPE:SOUND", "")/>
            </#if>
            <#if type.value?upper_case = "TEXT">
                <#assign TEXTUrl = type.url?replace("&qf=TYPE:VIDEO","")?replace("&qf=TYPE:IMAGE", "")?replace("&qf=TYPE:SOUND", "")/>
            </#if>
            <#if type.value?upper_case = "VIDEO">
                <#assign videoUrl = type.url?replace("&qf=TYPE:TEXT","")?replace("&qf=TYPE:IMAGE", "")?replace("&qf=TYPE:SOUND", "")/>
            </#if>
            <#if type.value?upper_case = "SOUND">
                <#assign audioUrl = type.url?replace("&qf=TYPE:VIDEO","")?replace("&qf=TYPE:IMAGE", "")?replace("&qf=TYPE:TEXT", "")/>
            </#if>
        </#list>
    </#if>
</#list>

<#list next as facet>
    <#if facet.type="TYPE">
        <#list facet.links as type>
            <#if type.remove><#assign showType = 1 /></#if>
        </#list>
    </#if>
    <#if facet.type="YEAR">
        <#list facet.links as date>
            <#if date.remove><#assign showYear = 1 /></#if>
        </#list>
    </#if>
    <#if facet.type="LANGUAGE">
        <#list facet.links as lang>
            <#if lang.remove><#assign showLanguage = 1 /></#if>
        </#list>
    </#if>
    <#if facet.type="PROVIDER">
        <#list facet.links as provider>
            <#if provider.remove><#assign showProvider = 1 /></#if>
        </#list>
    </#if>
    <#if facet.type="COUNTRY">
        <#list facet.links as country>
            <#if country.remove><#assign showCountry = 1 /></#if>
        </#list>
    </#if>
    <#if facet.type="USERTAGS">
        <#list facet.links as userTags>
            <#if userTags.remove><#assign showUserTags = 1 /></#if>
        </#list>
    </#if>
</#list>
<#assign servletUrl = servletUrl/>
<#include "inc_header.ftl">
<#include "inc_search_form.ftl">

<div id="doc4" class="yui-t2">
    <div id="hd">
        <#include "inc_top_nav.ftl"/>
    </div>
    <div id="bd">
        <div id="yui-main">
            <div class="yui-b">
                <div class="yui-g" id="search">

                    <@SearchForm "search_result"/>

                </div>

                <div class="yui-g" id="display">

                    <#include "inc_results_table.ftl"/>

                </div>
            </div>
        </div>
        <div class="yui-b">
            <center>
                <#include "inc_logo_sidebar.ftl"/>
            </center>

                <div id="leftOptions">
                    <#include "inc_facets_lists.ftl"/>
                </div>

        </div>
        <div id="ft">
            <#include "inc_footer.ftl"/>
        </div>
    </div>
</div>
</body>
</html>
</#compress>