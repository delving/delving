<#compress>
<#assign thisPage = "statistics.html"/>
<#include "includeMarcos.ftl"/>

<@addHeader "${portalDisplayName}", "",[],[]/>
<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script type="text/javascript">
function scriptPieChart(facetName, drawTarget, height, width, dataArray){
    // Load the Visualization API and the piechart package.
    google.load('visualization', '1', {'packages':['corechart']});

    // Set a callback to run when the Google Visualization API is loaded.
    google.setOnLoadCallback(drawChart);

    // Callback that creates and populates a data table,
    // instantiates the pie chart, passes in the data and
    // draws it.
    function drawChart() {

    // Create our data table.
      var data = new google.visualization.DataTable();
      data.addColumn('string', 'Name');
      data.addColumn('number', 'Count');
      data.addRows(dataArray);

      // Instantiate and draw our chart, passing in some options.
      var chart = new google.visualization.PieChart(document.getElementById(drawTarget));
      chart.draw(data, {width: width, height: height, is3D: false, title: facetName});
      // Drilll down into the generated iframe and over-ride the margins for better display control
      var topMargin = height/10;
      var margin = "-"+topMargin+"px 0 0 0";
      $("div#"+drawTarget+" iframe").css("height","380px");
      $("div#"+drawTarget+" iframe").contents().find('body').css("margin",margin);
    }

}
</script>
<style>
    div.facets_container {
        background: #fff;
        max-height: 250px;
        min-height: 100px;
        margin-bottom: 1em;
        padding:0;
        border: none;
    }
</style>

<section role="main" class="grid_12 main">

    <h1><@spring.message '_portal.ui.statistics' /></h1>
    <div class="grid_4 alpha">
        <h3 class="header"><@spring.message '_metadata.europeana.dataProvider'/></h3>
        <@createStatsRowsAndData "DATAPROVIDER" 400 600/>
    </div>
    <div class="grid_8 omega">
        <div id="chart-div-DATAPROVIDER"></div>
    </div>
    <hr/>
    <div class="grid_4 alpha">
        <h3 class="header"><@spring.message '_metadata.abm.county'/></h3>
        <@createStatsRowsAndData "COUNTY" 500 600/>
    </div>
    <div class="grid_8 omega">
        <div id="chart-div-COUNTY" class="chart-container"></div>
    </div>
    <hr/>
    <div class="grid_4 alpha">
        <h3 class="header"><@spring.message '_metadata.abm.municipality'/></h3>
        <@createStatsRowsAndData "MUNICIPALITY" 400 600/>
    </div>
    <div class="grid_8 omega">
        <div id="chart-div-MUNICIPALITY" class="chart-container"></div>
    </div>
    <hr/>
</section>

<@addFooter/>

</#compress>

<#macro createStatsRowsAndData facetName height=600 width=500>
    <#assign totalItems = getTotalCount (facetName)/>
    <#assign dataScript = ""/>
    <#assign dataRows = ""/>
    <#assign percentage = 0/>
    <#list facetMap.getFacet(facetName) as facet>

        <#-- SET PERCENTAGE PER PROVIDER FROM THE TOTAL OF ALL DATA-PROVIDERS -->
        <#assign x = facet.getCount() * 100/>
        <#assign y = totalItems/>
        <#assign percentage = (facet.getCount() * 100 / totalItems)/>

        <#-- CREATE TABLE ROWS -->
        <#assign dataRows>
             ${dataRows}<tr><td>${facet.getName()}</td><td>${facet.getCount()}</td></tr>
        </#assign>

        <#-- CREATE GOOGLE API DATA ARRAY -->
        <#if facet_has_next>
             <#assign dataScript = dataScript + "['" + facet.getName() + "'," + percentage +"],"/>
        <#else>
            <#assign dataScript = dataScript + "['" + facet.getName() + "'," + percentage +"]"/>
        </#if>

    </#list>

    <script type="text/javascript">
        var dataArray = [${dataScript}];
        scriptPieChart("${facetName}","chart-div-${facetName}", ${height}, ${width}, dataArray);
    </script>

    <#-- DATA TABLE -->
    <div class="facets_container">
        <table class="zebra" width="100%">
            <tbody>
                ${dataRows}
            </tbody>
        </table>
    </div>
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

