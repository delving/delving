<#import "spring.ftl" as spring />
<#assign thisPage = "sitemap.html"/>
<#compress>
<#include "inc_header.ftl">
 <div id="doc4" class="yui-t2">
    <div id="hd">
        <#include "inc_top_nav.ftl"/>
    </div>
    <#if entries??>
        <#list entries as entry>
            <p>
                <a href="${entry.loc}">${entry.name} (${entry.count})</a>
            </p>
        </#list>
    </#if>
 </div>   
</body>
</html>
</#compress>