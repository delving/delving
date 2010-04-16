<#import "spring.ftl" as spring />
<#assign thisPage = "sitemap.html"/>
<#compress>
<#include "inc_header.ftl">
 <div id="doc4" class="yui-t2">
    <div id="hd">
        <#include "inc_top_nav.ftl"/>
    </div>
    <div id="bd">
        <div id="yui-main">
            <div class="yui-b">
                <div class="yui-g" id="mainContent">
                    <h1><@spring.message 'Collections_t' /></h1>
                    
                    <div id="secondaryContent">

                        <div class="yui-u first">
                            <ul class="share-ideas">
                            <#if entries??>
                                <#list entries as entry>
                                    <li>
                                        <a href="${entry.loc}">${entry.name} (${entry.count})</a>
                                    </li>
                                </#list>
                            </#if>
                            </ul>
                        </div>

                    </div>


                </div>
            </div>
        </div>
                            
    </div>
 </div>   
</body>
</html>
</#compress>