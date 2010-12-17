<#compress>
<#assign thisPage = "statistics.html"/>
<#include "includeMarcos.ftl"/>

<@addHeader "${portalDisplayName}", "",[],[]/>

<style>
    h6 {font-weight: bold;margin:0 .25em;}
    div.facets_container {
        background: #fff;
        max-height: 165px;
        min-height: 100px;
        margin-bottom: 1em;
        padding:0;
    }
    div.graph {
        margin-top: 20px;
    }
</style>

<section role="main" class="grid_12 main">
    <h1><@spring.message '_portal.ui.statistics' /></h1>
    <div class="grid_6 alpha statistic"><@showStatisticsTable "PROVIDER"/></div>
    <div class="grid_6 omega graph"><@showStatisticsGraph "PROVIDER","pie"/></div>
    <div class="clear"></div>

    <div class="grid_6 alpha statistic"><@showStatisticsTable "DATAPROVIDER"/></div>
    <div class="grid_6 omega graph"><@showStatisticsGraph "DATAPROVIDER","pie"/></div>
    <div class="clear"></div>

    <div class="grid_6 alpha statistic"><@showStatisticsTable "COUNTY"/></div>
    <div class="grid_6 omega graph"><@showStatisticsGraph "COUNTY","bar"/></div>
    <div class="clear"></div>

    <div class="grid_6 alpha statistic"><@showStatisticsTable "MUNICIPALITY"/></div>
    <div class="grid_6 omega graph"><@showStatisticsGraph "MUNICIPALTIY","none"/></div>
    <div class="clear"></div>


</section>
<@addFooter/>

</#compress>

<#macro showStatisticsTable facetName>
<#attempt>
    <h6>${facetName}</h6>
    <div class="facets_container">
    <table class="zebra" width="100%">
        <caption></caption>
        <thead>
            <tr><th>Name</th><th>#items</th></tr>
        </thead>
        <#list facetMap.getFacet(facetName) as facet>
        <tr>
            <td>${facet.getName()}</td>
            <td>${facet.getCount()}</td>
        </tr>
        </#list>
    </table>
    </div>
<#recover>
asdfasdf 
</#attempt>
</#macro>

<#macro showStatisticsGraph facetName graphType>
<#attempt>
    <#assign totalItems = 1/>
    <#assign labels = ""/>
    <#assign data = ""/>
    <#list facetMap.getFacet(facetName) as facet>
        <#-- SET TOTAL NUMBER OF RECORDS BY ALL DATA-PROVIDERS -->
        <#assign totalItems = totalItems + facet.getCount()/>
        <#-- SET PERCENTAGE PER PROVIDER FROM THE TOTAL OF ALL DATA-PROVIDERS -->
        <#assign percentageData = ((facet.getCount()*100)/totalItems)/>
        <#-- CREATE DATA STRINGS FOR GOOGLE API -->
        <#if graphType="pie">
            <#if facet_has_next>
                 <#assign labels = labels + facet.getName() + "|"/>
                 <#assign data = data + percentageData?floor + ","/>
            <#else>
                <#assign labels = labels + facet.getName()/>
                <#assign data = data + percentageData?floor/>
            </#if>
        <#elseif graphType="bar">
            <#if facet_has_next>
                 <#assign labels = labels + "t" + facet.getName() + ",000000,0,"+ facet_index +",12|"/>
                 <#assign data = data + percentageData?floor + ","/>
            <#else>
                <#assign labels = labels + "t" + facet.getName() + ",000000,0,"+ facet_index +",12"/>
                <#assign data = data + percentageData?floor/>
            </#if>
        <#else>
             <@spring.message '_statistics.graph.not.rendered'/>
        </#if>
    </#list>
    <#if graphType="pie">
        <img src="http://1.chart.apis.google.com/chart?chs=460x120&amp;chd=t:${data}&amp;cht=p&amp;chl=${labels}"/>
    <#elseif graphType="bar">
        <img src="http://3.chart.apis.google.com/chart?chbh=a&amp;chs=400x200&amp;cht=bhs&amp;chd=t:${data}&amp;chm=${labels}"/>
    </#if>
<#recover>
    <@spring.message '_statistics.graph.not.rendered'/>
</#attempt>
</#macro>