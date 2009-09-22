<#import "spring.ftl" as spring />
<#assign formatTypes = formatTypes/>
<#assign thisPage = "explain.html">
<#assign view = "table">
<#include "inc_header.ftl">
<#include "inc_search_form.ftl">

<body>
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

                <h1>Ouput options and parameters</h1>

                    <dl>
                        <#list formatTypes as format>
                        <hr>
                        <dt>${format}</dt>
                        <dd>
                            Mime-Type : ${format.contentType}
                            <#if format.displaySpecified>
                            <br>Display : ${format.display}
                            </#if>
                        </dd>
                        </#list>
                    </dl>
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
