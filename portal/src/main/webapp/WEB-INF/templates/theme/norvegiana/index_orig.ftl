<#compress>
    <#assign thisPage = "index.html"/>
    <#include "includeMarcos.ftl"/>

<#--<@addHeader "${portalDisplayName}", "",["cycle/jquery.cycle.lite.min.js","index.js"],[]/>-->
<@addHeader "${portalDisplayName}", "",["tooltip.min.js","jcarousel/jquery.jcarousel.min.js","index.js"],[]/>

<#--<section id="leftbar" class="grid_3" role="complimentary">-->
       <#--&lt;#&ndash;<div class="slideshow">&ndash;&gt;-->
        <#--&lt;#&ndash;<#list randomItems as item>&ndash;&gt;-->
            <#--&lt;#&ndash;<a href="/${portalName}/record/${item.id}.html"><img src="${item.thumbnail}" /></a>&ndash;&gt;-->
        <#--&lt;#&ndash;</#list>&ndash;&gt;-->
        <#--&lt;#&ndash;</div>&ndash;&gt;-->
    <#--<div id="stats">-->
        <#--<style>-->
            <#--#stats h5#items {font-size: 5em; color: #ccc; position: relative; padding:0; margin: 0}-->
            <#--#stats h5#items span {font-size: .3em; color: #000; position: absolute; right: 10px; bottom: 5px;}-->
            <#--#stats h5#providers {font-size: 3em; color: #6699cc; position: relative; padding:10px 0 0 15px; margin: 0;}-->
            <#--#stats h5#providers span {font-size: .7em; color: #555; position: absolute; left: 0; top: 4px;}-->
        <#--</style>-->
        <#--<h5 id="items">items<span>178960</span></h5>-->
        <#--<h5 id="providers">providers<span>43</span></h5>-->
        <#--<h5 id="collections"><span>137</span></h5>-->
    <#--</div>-->
<#--</section>-->
<style type="text/css">

</style>

<section id="candy" class="grid_9 prefix_3">
        <ul id="mycarousel">
        <#list randomItems as item>
            <li><a href="/${portalName}/record/${item.id}.html""><img src="${item.thumbnail}" width="100" height="100" title="<@stringLimiter item.title 50/>"/></a></li>
        </#list>
        </ul>

</section>

<section id="info">

</section>

<@addFooter/>

</#compress>

