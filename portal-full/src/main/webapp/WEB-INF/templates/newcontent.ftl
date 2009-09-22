<#import "/spring.ftl" as spring />
<#assign thisPage = "new-content.html"/>
<#include "inc_header.ftl"/>

<div id="doc4" class="yui-t2">
    <div id="hd">
        <#include "inc_top_nav.ftl"/>
    </div>
   <div id="bd">
    <div id="yui-main">
        <div class="yui-b">
            <#if staticPagesSource?matches("database")>
                <#if staticPage?? >${staticPage.content}</#if>
                <#-- todo: add database call -->
            <#elseif staticPagesSource?matches("include")>
                <#--${message}-->
            </#if>
            <h1><@spring.message 'NewContent_t' /></h1>
<table id="tblContributors" class="tblList">
<tr>
<th>Country</th><th>Contributor</th>
</tr>
<tr><td>Germany</td><td><a href="http://www.landesarchiv-bw.de/" target="_blank">Landesarchiv Baden-W&#252;rttemberg</a></td></tr>
<tr><td>Germany</td><td><a href="http://www.slub-dresden.de" target="_blank">Die S&#228;chsische Landesbibliothek - Staats- und Universit&#228;tsbibliothek Dresden (SLUB)</a></td></tr>
<tr><td>Finland</td><td><a href="http://www.sls.fi" target="_blank">Svenska Litteraturs&#228;llskapet i Finland</a></td></tr>
<tr><td>Italy</td><td><a href="http://www.culturaitalia.it/pico/" target="_blank">CulturaItalia</a></td></tr>
<tr><td>Germany</td><td><a href="http://www.stadtgeschichtliches-museum-leipzig.de/" target="_blank">Stadtgeschichtliches Museum Leipzig</a></td></tr>
</table>

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

