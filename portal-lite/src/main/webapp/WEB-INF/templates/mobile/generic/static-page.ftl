<#import "/spring.ftl" as spring />
<#assign pageId = "sp"/>
<#assign thisPage = "static-page.html"/>
<#include "../inc_header.ftl"/>

<div id="logo">
	<a href="index.html"><img src="mobile/images/logo_slogan.png" alt="Logo"/></a>
</div>

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
<div id="content">
	<ul class="pageitem">
		<li class="textbox"
            <#if pageValue?? >
                 ${pageValue}
            <#else>
                <h2>Unable to find content for this page.</h2>
            </#if>
		</li>
		</ul>
</div>
<#include "../inc_footer.ftl"/>

