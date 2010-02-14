<#import "spring.ftl" as spring />
<#assign thisPage = "static-page.html"/>
<#include "inc_header.ftl"/>
<script>
// JavaScript Document
function ContactMe(prefix,suffix){
	var m =  Array(109,97,105,108,116,111,58);
	var s = '';
	for (var i = 0; i < m.length; i++){
		s += String.fromCharCode(m[i]);
	}
	window.location.replace(s + prefix + String.fromCharCode(8*8) + suffix);
	return false;
}

</script>
<div id="doc4" class="yui-t2">
    <div id="hd">
        <#include "inc_top_nav.ftl"/>
    </div>
   <div id="bd">
    <div id="yui-main">
        <div class="yui-b">

            <#if pageValue?? >
                 ${pageValue}
            <#else>
                <h2>Unable to find content for this page. </h2>
            </#if>


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

