<#import "spring.ftl" as spring />
<#assign query = ""/>
<#if RequestParameters.view??>
    <#assign view = "${RequestParameters.view}"/>
</#if>
<#if RequestParameters.query??>
    <#assign query = "${RequestParameters.query}"/>
</#if>
<#include "includeMarcos.ftl">

<@addHeader "${portalDisplayName}", "",[],[]/>

<section role="main" class="grid_12">

        <h1><@spring.message '_portal.ui.notification.error'/></h1>
        <br />
        <p>
            <@spring.message '_portal.ui.notification.registrationIsExpired'/>
            <@spring.message '_portal.ui.notification.pleaseRegisterAgain'/>

        </p>

</section>

<@addFooter/>

