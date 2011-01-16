<#compress>
    <#assign thisPage = "index.html"/>
    <#include "includeMarcos.ftl"/>

<@addHeader "${portalDisplayName}", "",["jcarousel/jquery.jcarousel.min.js","tooltip.min.js","index.js"],[]/>

<section class="grid_3 main" role="complimentary">

    <dl class="menu zebra">
        <dt>${portalDisplayName}</dt>
        <dd> total nr. of records: <strong>${totalCount?c}</strong></dd>
        <#if facetMap.facetExists("HASDIGITALOBJECT")>
            <dd>nr. of digital objects: <strong>${facetMap.getFacetValueCount("true", "HASDIGITALOBJECT")?c}</strong></dd>
        </#if>
        <#if facetMap.facetExists("DATAPROVIDER")>
            <dd><@spring.message '_metadata.searchfield.dataprovider'/>: <strong>${facetMap.getFacetCount("DATAPROVIDER")}</strong></dd>
        </#if>
    </dl>


</section>

<section id="main" class="grid_9" role="main">
    <#if randomItems??>
    <ul id="random-carousel">
    <#list randomItems as item>
        <#if useCache="true">
            <li><a href="/${portalName}/record/${item.id}.html"">
                    <img src="${cacheUrl}id=${item.thumbnail?url('utf-8')}" width="100" height="100" title="<@stringLimiter item.title 50/>" onerror="showDefaultImage(this,'${item.type}')"/>
                </a>
            </li>
            <#else>
                <li>
                    <a href="/${portalName}/record/${item.id}.html"">
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

