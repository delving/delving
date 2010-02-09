<#compress>
<h3><@spring.message 'RefineYourSearch_t' />:</h3>
    <#-- columsize is  used for the number of colums in the facet table. -->
<noscript>
                        <div class="attention">
                        <@spring.message 'NoScript_t' />
                        </div>
                    </noscript>
     <#list next as facet>
        <#assign togglerClass="toggler-c-closed"/>
        <#switch facet.type>
            <#case "LANGUAGE">
                <#if facet.links?size &gt; 0>
                   <#if facet.selected ><#assign togglerClass="toggler-c-opened"/></#if>
                   <div class="toggler-c ${togglerClass}" title="<@spring.message 'ByLanguage_t' /> ">
                   <noscript><h4><@spring.message 'ByLanguage_t' />:</h4></noscript>
                   <#assign columsize = 2>
                </#if>
                   <#break/>
            <#case "YEAR">
                <#if facet.links?size &gt; 0>
                    <#if facet.selected><#assign togglerClass="toggler-c-opened"/></#if>
                   <div class="toggler-c ${togglerClass}>" title="<@spring.message 'Bydate_t' />">
                   <noscript><h4><@spring.message 'Bydate_t' />:</h4></noscript>
                   <#assign columsize = 2>
               </#if>
               <#break/>
            <#case "TYPE">
                <#if facet.links?size &gt; 0>
                <#if facet.selected><#assign togglerClass="toggler-c-opened"/></#if>
               <div class="toggler-c ${togglerClass}" title="<@spring.message 'Bytype_t' />">
               <noscript><h4><@spring.message 'Bytype_t' />:</h4></noscript>
               <#assign columsize = 2>
               </#if>
               <#break/>
            <#case "PROVIDER">
                <#if facet.links?size &gt; 0>
                <#if facet.selected><#assign togglerClass="toggler-c-opened"/></#if>
               <div class="toggler-c ${togglerClass}" title="<@spring.message 'ByProvider_t' />">
               <noscript><h4><@spring.message 'ByProvider_t' />:</h4></noscript>
               <#assign columsize = 1>
               </#if>
               <#break/>
            <#case "COUNTRY">
                <#if facet.links?size &gt; 0>
                <#if facet.selected><#assign togglerClass="toggler-c-opened"/></#if>
               <div class="toggler-c ${togglerClass}" title="<@spring.message 'ByCountry_t' />">
                   <noscript><h4><@spring.message 'ByCountry_t' />:</h4></noscript>
                   <#assign columsize = 1>
                </#if>
               <#break/>
            <#--<#case "USERTAGS">-->
               <#--<#if facet.links?size &gt; 0>-->
                   <#--<#if facet.selected><#assign togglerClass="toggler-c-opened"/></#if>-->
                   <#--
                        <div class="toggler-c ${togglerClass}" title="<@spring.message 'ByUserTag_t' />">
                        <noscript><h4><@spring.message 'ByUserTag_t' />:</h4></noscript>

                   -->

               <#--<#assign columsize = 1>-->
               <#--</#if>-->
               <#--<#break/>-->
        </#switch>
        <#assign facet_max = 20/>
        <form id="form-refine-search" method="get">
            <input type="hidden" id="query-get" name="query" value=""/>
            <input type="hidden" id="qf-get" name="qf" value=""/>
        </form>
        <script>
            function refineSearch(query,qf){
               $("input#query-get").val(query);
                var strqf = $("input#qf-get").val(qf.replace("&qf=",""));
                //strqf = strqf.replace("&amp;","&");

               $("#form-refine-search").submit();
            }

        </script>
                   <style>
                       .ui-icon {float: left;}
                   </style>
        <#if facet.links?size &gt; 0>
            <div  style="width: 180px; overflow-x: hidden; overflow-y: auto; max-height: 200px;">
                <table class="facetTable" width="160px">
                    <#list facet.links?chunk(columsize) as row>
                        <tr>
                           <#list row as link>
                               <td align="left" style="padding: 2px;" class="ui-state-default ui-corner-all no-bg">
                                   <#-- DO NOT ENCODE link.url. This is already done in the java code. Encoding it will break functionality !!!  -->

                                   <#if !link.remove = true>
                                        <a href="${thisPage}?query=${query?html}${link.url?html}&view=${view}" title="${link.value}">
                                        <span class="ui-icon ui-icon-circle-plus"></span>
                                            <#--<input type="checkbox" value="" onclick="document.location.href='${thisPage}?query=${query?html}${link.url}';"/>-->
                                                <@stringLimiter "${link.value}" "20"/>(${link.count})
                                        </a>
                                <#else>
                                         <a href="${thisPage}?query=${query?html}${link.url?html}&view=${view}" title="${link.value}">
                                             <span class="ui-icon ui-icon-circle-minus"></span>
                                             <@stringLimiter "${link.value}" "20"/>
                                            <span class="fg-pink"> (${link.count})</span>
                                        </a>
                                </#if>
                                </td>
                           </#list>
                        </tr>
                    </#list>
                </table>
            </div>
        </div>
        </#if>
    </#list>
        <h3><@spring.message 'Actions_t'/>:</h3>
        <div class="related-links">
            <p class="linetop">
                <#if user??>
                    <a id="saveQuery" href="#" onclick="saveQuery('${queryToSave?url("utf-8")?js_string}', '${query?url("utf-8")?js_string}');"><@spring.message 'SaveThisSearch_t'/></a>
                <#else>
                    <a href="#" onclick="highLight('mustlogin');" class="disabled"><@spring.message 'SaveThisSearch_t'/></a>
                </#if>
            </p>
            <#if !user??>
            <div id="mustlogin" class="msg"><a href="secure/${thisPage}?${queryStringForPresentation}"><u><@spring.message 'LogIn_t'/></u></a> | <a href="secure/${thisPage}?${queryStringForPresentation}"><u><@spring.message 'Register_t'/></u></a></div>
            </#if>
            <div id="msg-save-search" class="msg-hide fg-pink"></div>
        </div>
        <div id="infoText">
            <img src="images/item-page.gif" width="35" alt="default image for texts"/>
            <img src="images/item-image.gif" width="35" alt="default image for images"/>
            <img src="images/item-video.gif" width="35" alt="default image for videos"/>
            <img src="images/item-sound.gif" width="35" alt="default image for sounds"/>
            <br />
            No image? Click through the colour block to see the item.  We are still loading...
        </div>
</#compress>