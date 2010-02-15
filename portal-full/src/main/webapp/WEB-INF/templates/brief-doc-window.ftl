<#import "spring.ftl" as spring />
<#assign thisPage = "brief-doc.html"/>
<#assign cacheUrl = cacheUrl/>
<#assign queryToSave = queryToSave />
<#assign typeUrl = typeUrl />
<#assign query = query />
<#assign view = "table"/>
<#if format??> <#assign format = format></#if>
<#if RequestParameters.view??> <#assign view = "${RequestParameters.view}"/></#if>
<#assign result = result/>
<#assign allCount = 0 />
<#assign textCount = 0 />
<#assign imageCount = 0 />
<#assign videoCount = 0 />
<#assign audioCount = 0 />
<#assign next = nextQueryFacets>
<#assign seq = briefDocs/>
<#compress>
<#list next as facet>
    <#if facet.type="TYPE">
        <#list facet.links as type>
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

        <div id="ft" class="${pageId}">
            <#include "inc_footer.ftl"/>
        </div>
    </div>
</div>
</body>
</html>
</#compress>