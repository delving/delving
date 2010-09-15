<#import "spring.ftl" as spring />
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
<#include "inc_header.ftl">



<div id="main" class="grid_16">

    <div class="exceptions" class="grid_8">


        <h2>Meldingen</h2>
        <div class="ui-widget">



        <#switch queryProblem>
            <#case 'MATCH_ALL_DOCS'>

                <div class="ui-state-error ui-corner-all" style="padding: 0pt 0.7em;">
                    <span class="ui-icon ui-icon-alert" style="float: left; margin-right: 0.3em;"></span>
                    <strong><@spring.message 'Error_t' />: </strong>
                    *:* invalid query <br/>
                    <strong>Please</strong> try another search.
                </div>


                <#break>
            <#case 'RECORD_NOT_FOUND'>
                <div class="ui-state-error ui-corner-all" style="padding: 0pt 0.7em;">
                    <span class="ui-icon ui-icon-alert" style="float: left; margin-right: 0.3em;"></span>
                    <strong><@spring.message 'Error_t' />: </strong>
                    <strong>Alert:</strong> Unable to find the requested Europeana Record.
                    <strong>Please</strong> try another search.
                </div>
                <#break>
            <#case 'RECORD_REVOKED'>
                <div class="ui-state-error ui-corner-all" style="padding: 0pt 0.7em;">
                    <span class="ui-icon ui-icon-alert" style="float: left; margin-right: 0.3em;"></span>
                    <strong><@spring.message 'Error_t' />: </strong>
                    <strong>Attention:</strong> This item has been withdrawn by the provider.
                    <strong>Please</strong> try another search.
                </div>
                <#break>
            <#case 'RECORD_NOT_INDEXED'>
                <div class="ui-state-error ui-corner-all" style="padding: 0pt 0.7em;">
                    <span class="ui-icon ui-icon-alert" style="float: left; margin-right: 0.3em;"></span>
                    <strong><@spring.message 'Error_t' />: </strong>
                    <strong>Attention:</strong> Requested Europeana Record is yet indexed.
                    <strong>Please</strong> try another search.
                </div>
                <#break>
            <#case 'TOKEN_EXPIRED'>
            <#case 'UNKNOWN_TOKEN'>
                <div class="ui-state-error ui-corner-all" style="padding: 0pt 0.7em;">
                    <span class="ui-icon ui-icon-alert" style="float: left; margin-right: 0.3em;"></span>
                    <strong><@spring.message 'Error_t' />: </strong>
                    The link you used to complete registration is invalid or has expired.
                    Please <a href="login.html">register your email again</a> to finish registration.
                </div>
                <#break>
            <#case 'MALFORMED_URL'>
                <div class="ui-state-error ui-corner-all" style="padding: 0pt 0.7em;">
                    <span class="ui-icon ui-icon-alert" style="float: left; margin-right: 0.3em;"></span>
                    <strong><@spring.message 'Error_t' />: </strong>
                    Unable to fullfill your request due to malformed query parameters.
                    <strong>Please</strong> try another search.
                </div>
                <#break>
            <#case 'SOLR_UNREACHABLE'>
                <div class="ui-state-error ui-corner-all" style="padding: 0pt 0.7em;">
                    <span class="ui-icon ui-icon-alert" style="float: left; margin-right: 0.3em;"></span>
                    <strong><@spring.message 'Error_t' />: </strong>
                    <strong>Alert:</strong> Unable to get a response from the Search Engine.
                    <strong>Please</strong> try another search later.
                </div>
                <#break>
            <#default>
                <div class="ui-state-error ui-corner-all" style="padding: 0pt 0.7em;">
                    <span class="ui-icon ui-icon-alert" style="float: left; margin-right: 0.3em;"></span>
                    <strong><@spring.message 'Error_t' />: </strong>
                    Something went wrong! An email has been sent to inform our technical staff.
                </div>
                <#if debug>
                    <div class="">
                        <h1>${queryProblem}</h1>

                        <h1>${exception}</h1>
                        <!--  The stack trace: is true -->
                    ${stackTrace}
                    </div>
                </#if>

        </#switch>

        </div>
    </div>
</div>

<div class="clear"></div>
<#include "inc_footer.ftl"/>


