<#import "spring.ftl" as spring />
<#compress>
    <#if idBeanList?? && fullViewUrl??>
        <#list idBeanList as idBean>
            <p>
                <a href="${fullViewUrl}?uri=${idBean.europeanaUri}">${fullViewUrl}?uri=${idBean.europeanaUri}</a>
            </p>
        </#list>
    </#if>
</body>
</html>
</#compress>