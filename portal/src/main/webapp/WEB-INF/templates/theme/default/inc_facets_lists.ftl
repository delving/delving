<#compress>
<h4><@spring.message 'RefineYourSearch_t' />:</h4>
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
                   <#if facet.selected><#assign togglerClass="toggler-c-opened"/></#if>
                   <div class="toggler-c ${togglerClass} ui-icon-circle-triangle-s" title="<@spring.message 'ByLanguage_t' /> ">
                   <noscript><h4><@spring.message 'ByLanguage_t' />:</h4></noscript>
                   <#assign columsize = 2>
                </#if>
                   <#break/>
            <#case "YEAR">
                <#if facet.links?size &gt; 0>
                    <#if facet.selected ><#assign togglerClass="toggler-c-opened"/></#if>
                   <div class="toggler-c ${togglerClass}>" title="<@spring.message 'Bydate_t' />">
                   <noscript><h4><@spring.message 'Bydate_t' />:</h4></noscript>
                   <#assign columsize = 2>
               </#if>
               <#break/>
            <#case "TYPE">
                <#if facet.links?size &gt; 0>
                <#if facet.selected ><#assign togglerClass="toggler-c-opened"/></#if>
               <div class="toggler-c ${togglerClass}" title="<@spring.message 'Bytype_t' />">
               <noscript><h4><@spring.message 'Bytype_t' />:</h4></noscript>
               <#assign columsize = 2>
               </#if>
               <#break/>
            <#case "PROVIDER">
                <#if facet.links?size &gt; 0>
                <#if facet.selected ><#assign togglerClass="toggler-c-opened"/></#if>
               <div class="toggler-c ${togglerClass}" title="<@spring.message 'ByProvider_t' />">
               <noscript><h4><@spring.message 'ByProvider_t' />:</h4></noscript>
               <#assign columsize = 1>
               </#if>
               <#break/>
            <#case "COUNTRY">
                <#if facet.links?size &gt; 0>
                <#if facet.selected ><#assign togglerClass="toggler-c-opened"/></#if>
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
    <#if seq?size &gt; 0>
        <h4><@spring.message 'Actions_t'/>:</h4>
        <div class="related-links">
            <p class="linetop">
                <#if user??>
                    <a id="saveQuery" href="inc_facets_lists.ftl#" onclick="saveQuery('SavedSearch', '${queryToSave?url("utf-8")?js_string}', '${query?url("utf-8")?js_string}');"><@spring.message 'SaveThisSearch_t'/></a>
                <#else>
                    <a href="inc_facets_lists.ftl#" onclick="highLight('mustlogin'); return false" class="disabled"><@spring.message 'SaveThisSearch_t'/></a>
                </#if>
            </p>
            <div id="msg-save-search" class="msg-hide fg-pink"></div>
        </div>
    </#if>
</#compress>
