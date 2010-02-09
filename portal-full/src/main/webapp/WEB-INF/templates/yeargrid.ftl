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
/*.yui-t2 .yui-b {float:left;width:18.4615em;*width:18.00em;}*/
.yui-t2 #yui-main .yui-b {margin-left:19.4615em;}
#bobframe {border: none;}
#timegrid {padding: 0; text-align: center; margin-bottom: 60px;}
#timegrid a, a:visited {text-decoration: none; color: #333; font-size: 14px;}
#timegrid a:hover {text-decoration: none; color: #ccc;}
#timegrid a:active {border: 4px solid #FF1493; color:#FF1493; }
#timegrid a:visited {color: #26a097;}
</style>
<script type="text/javascript">
    function showBob(year){
        var str = document.getElementById('query').value;
        document.getElementById("bobframe").src="bob.html?useCache="+${useCache}+"&query="+str;
    }
</script>



<div id="doc4" class="yui-t2">
    <div id="hd">
        <#include "inc_top_nav.ftl"/>
    </div>
   <div id="bd">
    <div id="yui-main">
        <div class="yui-b">

                <iframe src="bob.html?query=${bobQuery}&start=${start}" width="700" height="500" id="bobframe" scrolling="no" frameborder="no" name="bobframe"></iframe>

                <div id="timegrid">
                    <#assign facets = facetList/>
                    <#if facets??>
                        <#list facets as facet>
                             <#if facet.values?? && facet.name="YEAR">
                                 <#list facet.values?sort_by("name")?reverse as year>
                                     <#assign size = year.count/800>
                                     <#if size &lt; 12>
                                         <#assign size = 12/>
                                     <#elseif  size &gt; 100>
                                         <#assign size = 100/>
                                     </#if>
                                     <#if year.name != "0000">
                                        <#if !query?contains("*:*")>
                                              <a href="bob.html?query=${year.name}+${query}" target="bobframe" style="font-size:${size?ceiling}px;" title="${year.count} items" alt="${year.count} items">${year.name}</a>
                                        <#else>
                                              <a href="bob.html?query=${year.name}" target="bobframe" style="font-size:${size?ceiling}px;" title="${year.count} items" alt="${year.count} items">${year.name}</a>
                                        </#if>
                                    </#if>
                                 </#list>
                             </#if>
                        </#list>
                    </#if>
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

