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
                   <#if showLanguage = 1><#assign togglerClass="toggler-c-opened"/></#if>
                   <div class="toggler-c ${togglerClass}" title="<@spring.message 'ByLanguage_t' /> ">
                   <noscript><h4><@spring.message 'ByLanguage_t' />:</h4></noscript>
                   <#assign columsize = 2>
                </#if>
                   <#break/>
            <#case "YEAR">
                <#if facet.links?size &gt; 0>
                    <#if showYear = 1><#assign togglerClass="toggler-c-opened"/></#if>
                   <div class="toggler-c ${togglerClass}>" title="<@spring.message 'Bydate_t' />">
                   <noscript><h4><@spring.message 'Bydate_t' />:</h4></noscript>
                   <#assign columsize = 2>
               </#if>
               <#break/>
            <#case "TYPE">
                <#if facet.links?size &gt; 0>
                <#if showType = 1><#assign togglerClass="toggler-c-opened"/></#if>
               <div class="toggler-c ${togglerClass}" title="<@spring.message 'Bytype_t' />">
               <noscript><h4><@spring.message 'Bytype_t' />:</h4></noscript>
               <#assign columsize = 2>
               </#if>
               <#break/>
            <#case "PROVIDER">
                <#if facet.links?size &gt; 0>
                <#if showProvider = 1><#assign togglerClass="toggler-c-opened"/></#if>
               <div class="toggler-c ${togglerClass}" title="<@spring.message 'ByProvider_t' />">
               <noscript><h4><@spring.message 'ByProvider_t' />:</h4></noscript>
               <#assign columsize = 1>
               </#if>
               <#break/>
            <#case "COUNTRY">
                <#if facet.links?size &gt; 0>
                <#if showCountry = 1><#assign togglerClass="toggler-c-opened"/></#if>
               <div class="toggler-c ${togglerClass}" title="<@spring.message 'ByCountry_t' />">
                   <noscript><h4><@spring.message 'ByCountry_t' />:</h4></noscript>
                   <#assign columsize = 1>
                </#if>
               <#break/>
        </#switch>
        <#assign facet_max = 20/>

        <#if facet.links?size &gt; 0>
            <div  style="width: 100%; overflow-x: hidden; overflow-y: auto; max-height: 200px;">
                <table width="100%">
                    <#list facet.links?chunk(columsize) as row>
                        <tr>
                           <#list row as link>
                               <td align="left" style="padding: 2px;">
                                   <#-- DO NOT ENCODE link.url. This is already done in the java code. Encoding it will break functionality !!!  -->

                                   <#if !link.remove = true>
                                        <a class="add" href="${thisPage}?query=${query?html}${link.url?html}" title="${link.value}">
                                            <#--<input type="checkbox" value="" onclick="document.location.href='${thisPage}?query=${query?html}${link.url}';"/>-->
                                            <@stringLimiter "${link.value}" "25"/>(${link.count})
                                        </a>
                                <#else>
                                         <a class="remove" href="${thisPage}?query=${query?html}${link.url?html}" title="${link.value}">

                                             <@stringLimiter "${link.value}" "25"/>
                                             (${link.count})

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
                    <a id="saveQuery" href="#" onclick="saveQuery('SavedSearch', '${queryToSave?url("utf-8")?js_string}', '${query?url("utf-8")?js_string}');"><@spring.message 'SaveThisSearch_t'/></a>
                <#else>
                    <a href="#" onclick="highLight('mustlogin'); return false" class="disabled"><@spring.message 'SaveThisSearch_t'/></a>
                </#if>
            </p>
            <div id="msg-save-search" class="msg-hide fg-pink"></div>
        </div>
</#compress>
<#--
<#compress>
<h4><@spring.message 'RefineYourSearch_t' />:</h4>
    -->
<#-- columsize is  used for the number of colums in the facet table. -->
<#--
<noscript>
    <div class="attention">
    <@spring.message 'NoScript_t' />
    </div>
</noscript>

<form id="form-refine-search" method="get">
    <input type="hidden" id="query-get" name="query" value=""/>
    <input type="hidden" id="qf-get" name="qf" value=""/>
</form>

<div id="accordion">
     <#list next as facet>
        <#assign togglerClass="toggler-c-closed"/>
        <#switch facet.type>
            <#case "LANGUAGE">
                <#if facet.links?size &gt; 0>
                   <#if showLanguage = 1><#assign togglerClass="toggler-c-opened"/></#if>
                   <h3><a href="#"><@spring.message 'ByLanguage_t' />:</a></h3>
                   <#assign columsize = 2>
                </#if>
                   <#break/>
            <#case "YEAR">
                <#if facet.links?size &gt; 0>
                    <#if showYear = 1><#assign togglerClass="toggler-c-opened"/></#if>
                    <h3><a href="#"><@spring.message 'Bydate_t' />:</a></h3>
                   <#assign columsize = 2>
               </#if>
               <#break/>
            <#case "TYPE">
                <#if facet.links?size &gt; 0>
                <#if showType = 1><#assign togglerClass="toggler-c-opened"/></#if>
                <h3><a href="#"><@spring.message 'Bytype_t' /></a:</a></h3>
               <#assign columsize = 2>
               </#if>
               <#break/>
            <#case "PROVIDER">
                <#if facet.links?size &gt; 0>
                <#if showProvider = 1><#assign togglerClass="toggler-c-opened"/></#if>
               <h3><a href="#"><@spring.message 'ByProvider_t' />:</a></h3>
               <#assign columsize = 1>
               </#if>
               <#break/>
            <#case "COUNTRY">
                <#if facet.links?size &gt; 0>
                <#if showCountry = 1><#assign togglerClass="toggler-c-opened"/></#if>
                <h3><a href="#"><@spring.message 'ByCountry_t' />:</a></h3>
                   <#assign columsize = 1>
                </#if>
               <#break/>
        </#switch>

        <#assign facet_max = 20/>

        <#if facet.links?size &gt; 0>


            <div class="scroll">
                <table width="100%">
                    <#list facet.links?chunk(columsize) as row>
                        <tr>
                           <#list row as link>
                               -->
<#--<td align="left" style="padding: 2px;" class="ui-state-default ui-corner-all">-->
<#--
                            <td align="left" style="padding: 2px;" >
                                   -->
<#-- DO NOT ENCODE link.url. This is already done in the java code. Encoding it will break functionality !!!  -->
<#--

                                   <#if !link.remove = true>
                                        <a class="ui-state-default ui-corner-all" href="${thisPage}?query=${query?html}${link.url?html}" title="${link.value}">
                                        <span class="ui-icon ui-icon-circle-plus"></span>
                                            -->
<#--<input type="checkbox" value="" onclick="document.location.href='${thisPage}?query=${query?html}${link.url}';"/>-->
<#--
                                             <@stringLimiter "${link.value}" "20"/>
                                            (${link.count})
                                        </a>
                                <#else>
                                         <a class="ui-state-default ui-corner-all" href="${thisPage}?query=${query?html}${link.url?html}" title="${link.value}">
                                             <span class="ui-icon ui-icon-circle-minus"></span>
                                              <@stringLimiter "${link.value}" "20"/>
                                              (${link.count})
                                        </a>
                                </#if>
                                </td>
                           </#list>
                        </tr>
                    </#list>
                </table>
            </div>

        </#if>
    </#list>
</div>
        <h5><@spring.message 'Actions_t'/>:</h5>
        <div class="related-links">
            <p class="linetop">
                <#if user??>
                    <!---
                    todo: create a 'saveQuery' ftl page and replace # href with link & querystring to save the query incase javascript is turned off.
                    Try to get rid of all # in href's and practice non-obtrusive javascript
                    --->
<#--
                    <a id="saveQuery" href="#" onclick="saveQuery('${queryToSave?url("utf-8")?js_string}', '${query?url("utf-8")?js_string}'); return false;"><@spring.message 'SaveThisSearch_t'/></a>
                <#else>
                    <a href="login.html" onclick="highLight('mustlogin');return false" class="disabled"><@spring.message 'SaveThisSearch_t'/></a>
                </#if>
            </p>
            <#if !user??>
            <div id="mustlogin" class="msg"><a href="secure/${thisPage}?${queryStringForPresentation}"><@spring.message 'LogIn_t'/></a> | <a href="secure/${thisPage}?${queryStringForPresentation}"><@spring.message 'Register_t'/></a></div>
            </#if>
            <div id="msg-save-search" class="msg-hide fg-pink"></div>
        </div>

</#compress>-->
