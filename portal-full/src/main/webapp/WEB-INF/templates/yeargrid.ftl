<#import "spring.ftl" as spring />
<#assign thisPage = "year-grid.html"/>
<#include "inc_header.ftl"/>
<#assign query = query/>
<#if bobQuery??>
    <#assign bobQuery = bobQuery/>
<#else>
    <#assign bobQuery = query/>
</#if>
<#assign start = start/>
<style type="text/css">

.yui-t2 .yui-b {float:left;width:18.4615em;*width:18.00em;}
.yui-t2 #yui-main .yui-b {margin-left:19.4615em;}
#bobframe {border: none;}
#timegrid a, a:visited {text-decoration: none; color: #333;}
#timegrid a:hover {text-decoration: none; color: #ccc;}
</style>
<script type="text/javascript">
    function showBob(year){
        var str = document.getElementById('query').value;
        document.getElementById("bobframe").src="bob.html?useCache="+${useCache}+"&query="+str;
    }
</script>

<style>
    #timegrid a:active {border: 4px solid #FF1493; color:#FF1493; }
    #timegrid a:visited {color: #26a097;}
</style>
<body>

<div id="doc4" class="yui-t2">
    <div id="hd">
        <#include "inc_top_nav.ftl"/>
    </div>
   <div id="bd">
    <div id="yui-main">
        <div class="yui-b">

            <div style="border: 1px solid #ccc; padding: 10px; background: #eaeaea; margin: 40px 0 0 0; width: 700px;">

                <iframe src="bob.html?query=${bobQuery}&start=${start}" width="700" height="500" id="bobframe" scrolling="no" frameborder="no" name="bobframe"></iframe>

                <div id="timegrid" style="padding: 20px; text-align: center;">
                    <#assign facets = facetList/>
                    <#if facets?exists>
                        <#list facets as facet>
                             <#if facet.type="YEAR">
                                 <#list facet.counts?sort_by("value")?reverse as year>
                                     <#assign size = year.count/800>
                                     <#if size &lt; 12>
                                         <#assign size = 12/>
                                     <#elseif  size &gt; 100>
                                         <#assign size = 100/>
                                     </#if>
                                     <#if year.value != "0000">
                                        <#if !query?contains("*:*")>
                                              <a href="bob.html?query=${year.value}+${query}" target="bobframe" style="font-size:${size?ceiling}px;" title="${year.count} items" alt="${year.count} items">${year.value}</a>
                                        <#else>
                                              <a href="bob.html?query=${year.value}" target="bobframe" style="font-size:${size?ceiling}px;" title="${year.count} items" alt="${year.count} items">${year.value}</a>
                                        </#if>
                                    </#if>
                                 </#list>
                             </#if>
                        </#list>
                    </#if>
                </div>

            </div>

        </div>

    </div>
        <div class="yui-b">
            <#include "inc_logo_sidebar.ftl"/>
            <div  id="leftOptions" style="padding: 10px 5px; min-height: 200px;">
                <#-- !! used get method instead of showBob to prevent internal redirect that triggers cookie theft warning in FireFox3 -->
                <form method="get" action="year-grid.html" accept-charset="UTF-8" onsubmit="return checkFormSimpleSearch('query');">
                <h3><@spring.message 'BrowseThroughTime_t'/>:</h3>
                    <input  class="text" type="text" maxlength="25" name="query" id="query"/>
                    <input type="submit" class="button" value="<@spring.message 'Search_t'/>"/>
                </form>
            </div>
        </div>
    </div>
   <div id="ft">
	   <#include "inc_footer.ftl"/>
   </div>
</div>
</body>
</html>

