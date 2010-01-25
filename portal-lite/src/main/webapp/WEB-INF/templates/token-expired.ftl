<#import "spring.ftl" as spring />
<#assign thisPage = "sitemap.html">
<#assign view = "table"/>
<#assign query = ""/>
<#if RequestParameters.view?exists>
    <#assign view = "${RequestParameters.view}"/>
</#if>
<#if RequestParameters.query?exists>
    <#assign query = "${RequestParameters.query}"/>
</#if>
<#include "spring_form_macros.ftl"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
   <title>Europeana - Token Expired</title>
   <link rel="stylesheet" href="yui/build/reset-fonts-grids/reset-fonts-grids.css" type="text/css"/>
   <link rel="stylesheet" href="/css/common.css" type="text/css"/>
   <link rel="stylesheet" href="/css/login-register.css" type="text/css"/>
</head>
<body onload="">
<div id="doc4" class="yui-t2">
    <div id="hd">
        <#include "inc_top_nav.ftl"/>
    </div>
   <div id="bd">
    <div id="yui-main">
        <div class="yui-b">
            <div class="yui-g" id="mainContent">

                <h1>Error</h1>
                <br />
                <p>
                    The link you used to complete registration is invalid or has expired.
                    Please <a href="login.html">register your email again</a> to finish registration.
                </p>



           </div>
        </div>
    </div>
        <div class="yui-b">
            <a href="index.html"><img src="images/logo03.gif" alt="logo Europeana think culture" title="logo Europeana think culture" /></a>

        </div>
    </div>
   <div id="ft">
	    <#include "inc_footer.ftl"/>
   </div>
</div>
</body>
</html>
