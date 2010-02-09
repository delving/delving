<#import "/spring.ftl" as spring />
<#assign thisPage = "advancedsearch.html"/>

<#assign view = "table">
<#if RequestParameters.view?exists><#assign view = "${RequestParameters.view}"></#if>
<#include "inc_header.ftl"/>
<#include "inc_search_form.ftl">
<div id="doc4" class="yui-t2">
    <div id="hd">
        <#include "inc_top_nav.ftl"/>
    </div>
   <div id="bd">
    <div id="yui-main">
        <div class="yui-b">

        <div  id="search"><@SearchForm "search_home"/></div>

        </div>

    </div>
        <div class="yui-b">
            <#include "inc_logo_sidebar.ftl"/>
        </div>
    </div>
   <div id="ft">
	   <#include "inc_footer.ftl"/>
   </div>
</div>
</body>
</html>

