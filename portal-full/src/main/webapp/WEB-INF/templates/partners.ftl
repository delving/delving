<#import "/spring.ftl" as spring />
<#assign thisPage = "partners.html"/>
<#include "inc_header.ftl"/>
<style>
    .tblList th {
        font-weight: bold;
        color: #fff;
        background: #666;
        text-align: center;
        border-right: 1px solid #fff;
        padding: 0.25em;
        font-size: 1em;
    }
    .tblList td {
        font-size: .85em;
        line-height: 1.5em;
    }
    #tblContributors th {
        background: #26a097;
    }
    #tblPartners th {
        background: #FF1493;
    }
    .tblList td {
        padding: 0.25em;
        border-bottom: 1px solid #ccc;
    }
</style>
<body class=" yui-skin-sam">


<div id="doc4" class="yui-t2">
    <div id="hd">
        <#include "inc_top_nav.ftl"/>
    </div>
   <div id="bd">
    <div id="yui-main">
        <div  class="yui-b">
        <div  class="yui-g">
            <div class="yui-g first">
            <h1><@spring.message 'Partners_t' /></h1>
                <#if partnerPagesSource?matches("database")>
                    <#include "db_partners.ftl">
                <#elseif partnerPagesSource?matches("include")>
                    <#include "list_partners.ftl">
                </#if>
            </div>
            <div class="yui-g">
            <h1><@spring.message 'Contributors_t' /></h1>
                <#if partnerPagesSource?matches("database")>
                    <#include "db_contributors.ftl">
                <#elseif partnerPagesSource?matches("include")>
                    <#include "list_contributors.ftl">
                </#if>
            </div>
        </div>
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

