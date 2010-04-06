<#import "spring.ftl" as spring />
<#assign thisPage = "sitemap.html"/>
<#compress>
<#include "inc_header.ftl">
    <#if idBeanList?? && fullViewUrl??>
        <#list idBeanList as idBean>
            <p>
                <a href="${fullViewUrl}?uri=${idBean.europeanaUri}">${idBean.europeanaUri}</a>
            </p>
        </#list>
    </#if>
</body>
</html>
</#compress>
