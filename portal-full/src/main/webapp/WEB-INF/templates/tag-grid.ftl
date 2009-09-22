<#import "/spring.ftl" as spring />
<#assign thisPage = "tag-grid.html"/>
<#include "inc_header.ftl"/>
<style type="text/css">

.yui-t2 .yui-b {float:left;width:18.4615em;*width:18.00em;}
.yui-t2 #yui-main .yui-b {margin-left:19.4615em;}
#bobframe {border: none;}
#timegrid a, a:visited {text-decoration: none; color: #333;}
#timegrid a:hover {text-decoration: none; color: #ccc;}
</style>
<script type="text/javascript">
    function showBob(tag){
        document.getElementById("bobframe").src="bob.html?query=USERTAGS:\""+tag+"\"";
    }
</script>


<body>

<div id="doc4" class="yui-t2">
    <div id="hd">
        <#include "inc_top_nav.ftl"/>
    </div>
   <div id="bd">
    <div id="yui-main">
        <div class="yui-b">

            <div style="border: 1px solid #ccc; padding: 10px; background: #eaeaea; margin: 40px 0 0 0; width: 700px !important; width: 690px;">

                <iframe src="bob.html?qt=tags" width="690" height="500" id="bobframe" scrolling="no" frameborder="no" name="bob"></iframe>

                <div id="timegrid" style="padding: 20px; text-align: center;">
                    <#assign facets = facetList/>
                    <#if facets?exists>
                        <#list facets as facet>
                             <#if facet.type="USERTAGS">
                                 <#list facet.counts?sort_by("value")?reverse as tag>
                                     <#assign size = tag.count/100>
                                     <#if size &lt; 10>
                                         <#assign size = 10/>
                                     <#elseif  size &gt; 100>
                                         <#assign size = 100/>
                                     </#if>
                                     <#if tag.value != "0000">
                                     <a href="javascript:showBob('${tag.value}')" style="font-size:${size?ceiling}px;">${tag.value}</a>
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
            <a href="index.html"><img src="images/logo-sm.gif" alt="logo Europeana think culture" title="logo Europeana think culture" /></a>
        </div>
    </div>
   <div id="ft">
	   <#include "inc_footer.ftl"/>
   </div>
</div>
</body>
</html>

