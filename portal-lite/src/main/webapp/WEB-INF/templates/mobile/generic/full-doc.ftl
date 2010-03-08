<#import "/spring.ftl" as spring />
<#assign model = result/>
<#assign result = result/>
<#assign uri = uri>
<#assign view = "table"/>
<#assign thisPage = "full-doc.html"/>
<#compress>
    <#if startPage??><#assign startPage = startPage/></#if>
    <#if RequestParameters.view??> <#assign view = "${RequestParameters.view}"/></#if>
    <#if format??><#assign format = format/></#if>
    <#if pagination??>
        <#assign pagination = pagination/>
        <#assign queryStringForPaging = pagination.queryStringForPaging />
    </#if>

    <#assign tab = ""/>
    <#if RequestParameters.tab?exists>
        <#assign tab = RequestParameters.tab/>
    </#if>

<#-- Removed ?url('utf-8') from query assignment -->
    <#if RequestParameters.query??><#assign query = "${RequestParameters.query}"/></#if>
    <#include "../inc_header.ftl">
<div id="logo">
	<a href="index.html"><img src="mobile/images/logo_slogan.png" alt="Logo"/></a>
</div>
<div id="viewselectnav">
                    <#if pagination?? && pagination.returnToResults?? && view?? && query?? && query?length &gt; 0 >
                        <a href="${pagination.returnToResults?html}">
                            <#switch view>
                                <#case "text_only"><img class="titleresultnav"
                                                        src="mobile/images/text_only.gif"/><#break/>
                                <#case "image_only"><img class="titleresultnav"
                                                         src="mobile/images/image_only.gif"/><#break/>
                                <#default><#case "mixed"><img class="titleresultnav"
                                                              src="mobile/images/mixed.gif"/><#break/>
                            </#switch>
                        </a>
                    </#if>
                    <a href="index.html"><img alt="home" src="mobile/iwebkit/images/home.png" style="height: 16px" /></a>
</div>

<div id="content">
    <#include "../inc_result_table_full.ftl"/>
</div>


<div id="resultnavigation">
    <@resultnavigation/>
</div>
    <#include "../inc_footer.ftl"/>

</#compress>