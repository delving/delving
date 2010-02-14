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
<#include "inc_search_form.ftl">

<div id="doc4" class="yui-t2">
    <div id="hd">
        <#include "inc_top_nav.ftl"/>
    </div>
   <div id="bd">
    <div id="yui-main">
        <div class="yui-b">
                <div class="yui-g" id="search">

                    <@SearchForm "search_result"/>

                </div>
                <div class="yui-g">
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
                        <#case 'MALFORMED_QUERY'>
                                <div class="ui-widget ui-error">
                            Unable to fulfill your request due to a malformed query syntax.
                            <strong>Please</strong> try another search.
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
                    <!-- general exception page -->
                <#--<p style="margin: 0 0 240px 0">Please try another search.</p>-->
            </div>
        </div>
        <div class="yui-b">
           <a href="index.html"><img src="images/logo-sm.gif" alt="logo Europeana think culture" title="logo Europeana think culture" /></a>
        </div>
   </div>
   <div id="ft">
	    <#include "inc_footer.ftl"/>
   </div>
</div>
</body>
</html>

