<#assign queryProblem = queryProblem>
<#assign exception = exception>
<#assign stackTrace = stackTrace>
<#assign thisPage = "exception.html">
<#assign pageId = "excp">
<#assign view = "table"/>
<#assign query = ""/>

<#if RequestParameters.view??>
    <#assign view = "${RequestParameters.view}"/>
</#if>
<#if RequestParameters.query??>
    <#assign query = "${RequestParameters.query}"/>
</#if>
<#include "includeMarcos.ftl">

<@addHeader "Delving", "",[],[]/>


<section role="main" class="grid_4 prefix_4">

<h1>${portalDisplayName}</h1>

<#switch queryProblem>
    <#case 'RECORD_NOT_FOUND'>
       <div class="ui-widget ui-error">
       <@spring.message '_portal.ui.notification.recordNotFound'/>
       </div>
     <#break>
     <#case 'RECORD_REVOKED'>
        <div class="ui-widget ui-info">
            <@spring.message '_portal.ui.notification.recordRevoked'/>
        </div>
    <#break>
    <#case 'RECORD_NOT_INDEXED'>
        <div class="ui-widget ui-info">
            <@spring.message '_portal.ui.notification.recordNotIndexed'/>
        </div>
    <#break>
    <#case 'TOKEN_EXPIRED'>
    <#case 'UNKNOWN_TOKEN'>
        <div class="ui-widget ui-info">
        <@spring.message '_portal.ui.notification.registrationIsExpired'/>
        <@spring.message '_portal.ui.notification.pleaseRegisterAgain'/>
        </div>
    <#break>
    <#case 'MALFORMED_URL'>
        <div class="ui-widget ui-error">
        <@spring.message '_portal.ui.notification.malformedQuery'/>
        <@spring.message '_portal.ui.notification.tryAnotherSearch'/>
        </div>
    <#break>
    <#case 'SOLR_UNREACHABLE'>
        <div class="ui-widget ui-error">
        <@spring.message '_portal.ui.notification.solrUnreachable'/>
        </div>
    <#break>
    <#default>
        <div class="ui-widget ui-info">
            <@spring.message '_portal.ui.notification.generalErrorMessage'/>
            <@spring.message '_portal.ui.notification.tryAnotherSearch'/>
        </div>
        <#if debug>
            <div class="yui-u first">
                <h1>${queryProblem}</h1>
                <h1>${exception}</h1>
                <!--  The stack trace: is true -->
                    ${stackTrace}
            </div>
        </#if>    
</#switch>

</section>

<@addFooter/>

