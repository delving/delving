<#import "spring.ftl" as spring />
<#assign thisPage = "sitemap.html"/>
<#include "inc_header.ftl">

<#compress>
    <#if entries??>
        <#list entries as entry>
            <p>
                <a href="${entry.loc}">${entry.name} (${entry.count})</a>
            </p>
        </#list>
    </#if>
</body>
</html>
</#compress>