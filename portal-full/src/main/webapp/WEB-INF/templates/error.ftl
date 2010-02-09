<#import "spring.ftl" as spring />
<#assign thisPage = "exception.html">
<#assign pageId = "exc"/>
<#assign view = "table"/>
<#assign query = ""/>
<#if RequestParameters.view?exists>
    <#assign view = "${RequestParameters.view}"/>
</#if>
<#if RequestParameters.query?exists>
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

                <h3 style="margin: 60px 0 20px 0">Something went wrong! An email has been sent to inform our technical staff.</h3>
                <p style="margin: 0 0 240px 0">Please try another search.</p>

               <#--<#switch queryProblem>-->
               <#--<#case 'MATCH_ALL_DOCS'>-->
               <#--*:* invalid query-->
               <#--<#break>-->
               <#--<#default>-->
                <#--<div class="yui-u first">-->
                    <#--<h1>${queryProblem}</h1>-->
                    <#--<h1>${exception}</h1>-->
                    <#--<!--  The stack trace: -->
                    <#--${stackTrace}-->
                <#--</div>-->
               <#--</#switch>-->

               </div>
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

