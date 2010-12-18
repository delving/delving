<#compress>
<#assign thisPage = "statistics.html"/>
<#include "includeMarcos.ftl"/>

<@addHeader "${portalDisplayName}", "",[],[]/>

<style>
    h6 {font-weight: bold;margin:0 .25em;text-transform:uppercase;}
    div.facets_container {
        background: #fff;
        max-height: 165px;
        min-height: 100px;
        margin-bottom: 1em;
        padding:0;
    }
    div.graph {
        margin-top: 1.5em;
    }
</style>

<section role="main" class="grid_12 main">
    <h1><@spring.message '_portal.ui.statistics' /></h1>
    <div class="grid_4 alpha statistic"><@showStatisticsTable "PROVIDER"/></div>
    <div class="grid_8 omega graph"><@showStatisticsGraph "PROVIDER","pie"/></div>
    <div class="clear"></div>

    <div class="grid_4 alpha statistic"><@showStatisticsTable "DATAPROVIDER"/></div>
    <div class="grid_8 omega graph"><@showStatisticsGraph "DATAPROVIDER","pie"/></div>
    <div class="clear"></div>

    <div class="grid_4 alpha statistic"><@showStatisticsTable "COUNTY"/></div>
    <div class="grid_8 omega graph"><@showStatisticsGraph "COUNTY","pie","600x180"/></div>
    <div class="clear"></div>

    <div class="grid_4 alpha statistic"><@showStatisticsTable "MUNICIPALITY"/></div>
    <div class="grid_8 omega graph"><@showStatisticsGraph "MUNICIPALITY","none"/></div>
    <div class="clear"></div>


</section>
<@addFooter/>

</#compress>

<#macro showStatisticsTable facetName>
    <h6>
    <#switch facetName>
        <#case "PROVIDER"><@spring.message '_metadata.europeana.provider'/><#break/>
        <#case "DATAPROVIDER"><@spring.message '_metadata.europeana.dataProvider'/><#break/>
        <#case "COUNTY"><@spring.message '_metadata.abm.county'/><#break/>
        <#case "MUNICIPALITY"><@spring.message '_metadata.abm.municipality'/><#break/>
        <#default>${facetName}<#break/>
    </#switch>
    </h6>
    <#attempt>
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
        <p><@spring.message '_statistics.graph.not.rendered'/></p>
    </#attempt>
</#macro>

<#macro showStatisticsGraph facetName graphType graphSize="600x100">
<#attempt>
    <#assign totalItems = getTotalCount (facetName)/>
    <#assign labels = ""/>
    <#assign data = ""/>
    <#assign percentage = 0/>

    <#list facetMap.getFacet(facetName) as facet>
        <#-- SET PERCENTAGE PER PROVIDER FROM THE TOTAL OF ALL DATA-PROVIDERS -->
        <#assign x = facet.getCount() * 100/>
        <#assign y = totalItems/>
        <#assign percentage = (facet.getCount() * 100 / totalItems)/>

        <#-- CREATE DATA STRINGS    FOR GOOGLE API -->
        <#if graphType="pie">
            <#if facet_has_next>
                 <#assign labels = labels + facet.getName() + " (" + percentage +"%)|"/>
                 <#assign data = data + percentage + ","/>
            <#else>
                <#assign labels = labels + facet.getName() + " (" +percentage +"%)"/>
                <#assign data = data + percentage/>
            </#if>
        <#elseif graphType="bar">
            <#if facet_has_next>
                 <#assign labels = labels + "t" + facet.getName() + " (" + percentage +"%)" + ",000000,0,"+ facet_index +",12|"/>
                 <#assign data = data + percentage + ","/>
            <#else>
                <#assign labels = labels + "t" + facet.getName() + " (" + percentage +"%)" + ",000000,0,"+ facet_index +",12"/>
                <#assign data = data + percentage/>
            </#if>

        </#if>
    </#list>
    <#if graphType="pie">
        <img src="http://1.chart.apis.google.com/chart?chs=${graphSize}&amp;chd=t:${data}&amp;cht=p&amp;chl=${labels}"/>
        <#--<iframe src="http://1.chart.apis.google.com/chart?chs=${graphSize}&amp;chd=t:${data}&amp;cht=p&amp;chl=${labels}&amp;chof=validate"></iframe>-->
    <#elseif graphType="bar">
        <img src="http://3.chart.apis.google.com/chart?chbh=a&amp;chs=400x200&amp;cht=bhs&amp;chd=t:${data}&amp;chm=${labels}"/>
        <#--<iframe src="http://3.chart.apis.google.com/chart?chbh=a&amp;chs=400x200&amp;cht=bhs&amp;chd=t:${data}&amp;chm=${labels}&amp;chof=validate"></iframe>-->
        <#elseif graphType="none">
            <p><p><@spring.message '_statistics.graph.not.rendered'/></p></p>
    </#if>

<#recover>

    <p><@spring.message '_statistics.graph.not.rendered'/></p>
</#attempt>                                 
</#macro>

<#function getTotalCount facet>
    <#assign total = 0/>
    <#if facetMap.facetExists(facet)>
          <#list facetMap.getFacet(facet) as facetInstance>
               <#assign total = total + facetInstance.getCount()/>
           </#list>
          <#return total/>
    <#else>
          <#return "false">
    </#if>
</#function>