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
<#--<#include "inc_search_form.ftl">-->

<body>
<div id="sidebar" class="grid_3">

    <div id="identity">
            <h1>Europeana Lite</h1>
            <a href="index.html" title="Europeana lite"><img src="images/europeana_open_logo_small.jpg" alt="European Open Source"/></a>
    </div>

</div>

<div id="main" class="grid_9">

    <div id="top-bar">

        <#include "language_select.ftl">
    </div>

    <div class="clear"></div>

                       <#switch queryProblem>
                       <#case 'MATCH_ALL_DOCS'>
                        <div class="ui-widget ui-error">
                            *:* invalid query<br/>
                        </div>
                        <div class="ui-widget ui-info"><strong>Please</strong> try another search.</div>
                       <#break>
                        <#case 'RECORD_NOT_FOUND'>
                           <div class="ui-widget ui-error">
                           <strong>Alert:</strong> Unable to find the requested Europeana Record. <strong>Please</strong> try another search.
                           </div>
                         <#break>
                         <#case 'RECORD_REVOKED'>
                            <div class="ui-widget ui-info">
                            <strong>Attention:</strong> This item has been withdrawn by the provider. <strong>Please</strong> try another search.
                            </div>
                        <#break>
                        <#case 'RECORD_NOT_INDEXED'>
                            <div class="ui-widget ui-info">
                            <strong>Attention:</strong> Requested Europeana Record is yet indexed. <strong>Please</strong> try another search.
                            </div>
                        <#break>
                        <#case 'TOKEN_EXPIRED'>
                        <#case 'UNKNOWN_TOKEN'>
                            <div class="ui-widget ui-info">
                            The link you used to complete registration is invalid or has expired.
                            Please <a href="login.html">register your email again</a> to finish registration.
                            </div>
                        <#break>
                        <#case 'MALFORMED_URL'>
                            <div class="ui-widget ui-error">
                            Unable to fullfill your request due to malformed query parameters.
                            <strong>Please</strong> try another search.
                            </div>
                        <#break>
                        <#case 'SOLR_UNREACHABLE'>
                            <div class="ui-widget ui-error">
                                <strong>Alert:</strong> Unable to get a response from the Search Engine. <strong>Please</strong> try another search later.
                            </div>
                        <#break>
                       <#default>
                            <div class="ui-widget ui-info">
                                Something went wrong! An email has been sent to inform our technical staff.
                                <strong>Please</strong> try another search.
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
 </div>

	    <#include "inc_footer.ftl"/>

</body>
</html>

