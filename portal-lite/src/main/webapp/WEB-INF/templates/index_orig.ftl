<#compress>
<#assign thisPage = "index.html">
<#assign pageId = "in">

<#include "inc_header.ftl">



<div id="main" class="grid_12">

    <div class="grid_3">
        <@userbar/>
     </div>

    <div class="clearfix"></div>

    <div class="grid_3">&nbsp;</div>
    
        <noscript>
            <div class="ui-widget grid_5 alpha">
                <div class="ui-state-highlight ui-corner-all" style="padding: 0pt 0.7em; margin-top: 20px;">
                    <@spring.message 'NoScript_t' />
                </div>
            </div>
        </noscript>

    </div><!-- end search -->

</div>
<#include "inc_footer.ftl"/>
</#compress>

