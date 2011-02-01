<#compress>
    <#assign thisPage = "index.html"/>
    <#include "includeMarcos.ftl"/>

<@addHeader "${portalDisplayName}", "",["jcarousel/jquery.jcarousel.min.js","tooltip.min.js","index.js"],[]/>

<section class="grid_3 main" role="complementary">

    <dl class="menu zebra" id="statistics">
        <dt>${portalDisplayName}</dt>
        <dd><@spring.message '_portal.ui.statistics.records'/>: <strong><a href="/${portalName}/search?query=*:*">${totalCount?c}</a></strong></dd>
        <#if facetMap.facetExists("HASDIGITALOBJECT")>
            <dd><@spring.message '_portal.ui.statistics.objects'/>: <strong><a href="/${portalName}/search?query=*:*&amp;qf=HASDIGITALOBJECT:true">${facetMap.getFacetValueCount("true", "HASDIGITALOBJECT")?c}</a></strong></dd>
        </#if>
        <#if facetMap.facetExists("DATAPROVIDER")>
            <dd><@spring.message '_portal.ui.statistics.providers'/>: <strong><a href="/${portalName}/search?query=*:*">${facetMap.getFacetCount("DATAPROVIDER")}</a></strong></dd>
        </#if>
        <dd>
            <a href="/${portalName}/statistics.html"><@spring.message '_portal.ui.statistics'/></a></dd>
    </dl>


</section>

<section id="main" class="grid_9" role="main">
    <#if randomItems??>
    <ul id="random-carousel">
    <#list randomItems as item>
        <#if useCache="true">
            <li><a href="/${portalName}/object/${item.id}.html">
                    <img src="${cacheUrl}id=${item.thumbnail?url('utf-8')}" width="100" height="100" title="<@stringLimiter item.title 50/>" onerror="showDefaultImage(this,'${item.type}')"/>
                </a>
            </li>
            <#else>
                <li>
                    <a href="/${portalName}/object/${item.id}.html">
                        <img src="${item.thumbnail}"  width="100" height="100" title="<@stringLimiter item.title 50/>" onerror="showDefaultImage(this,'${item.type}')"/>
                    </a>
                </li>
        </#if>
    </#list>
    </ul>

    </#if>
    <div id="info">
    <#-- dynamic cms content placed here -->
    </div>
</section>

<@addFooter/>

</#compress>

