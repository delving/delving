<#compress>
    <#assign thisPage = "index.html"/>
    <#include "includeMarcos.ftl"/>

<@addHeader "${portalDisplayName}", "",["tooltip.min.js","jcarousel/jquery.jcarousel.min.js","index.js"],[]/>

<section class="grid_3 main" role="complimentary">

    <dl class="menu">
        <dt>${portalDisplayName}</dt>
        <dd> total number of items: <strong>${totalCount?c}</strong></dd>
        <#if facetMap.facetExists("DATAPROVIDER")>
        <dd><@spring.message 'dataproviders'/>: <strong>${facetMap.getFacetCount("DATAPROVIDER")}</strong></dd>
        </#if>
     </dl>


</section>

<section id="main" class="grid_9" role="main">

    <ul id="random-carousel">
    <#list randomItems as item>
        <li><a href="/${portalName}/record/${item.id}.html""><img src="${item.thumbnail}" width="100" height="100" title="<@stringLimiter item.title 50/>"/></a></li>
    </#list>
    </ul>

    <div id="info">
    <#-- dynamic cms content placed here -->
    </div>
</section>

<@addFooter/>

</#compress>

