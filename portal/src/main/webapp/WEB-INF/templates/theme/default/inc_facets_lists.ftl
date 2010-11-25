<#compress>
<h4><@spring.message '_action.refine.your.search' />:</h4>
<noscript>
    <div class="attention">
    <@spring.message 'NoScript_t' />
    </div>
</noscript>
<#--columsize is  used for the number of colums in the facet table. -->

<#assign columsize = 2>
    <#list next as facet>
         <#assign togglerClass="toggler-c-closed"/>
        <#switch facet.type>
            <#case "LANGUAGE">
                <#if facet.links?size &gt; 0>
                   <#if facet.selected><#assign togglerClass="toggler-c-opened"/></#if>
                   <div class="toggler-c ${togglerClass} ui-icon-circle-triangle-s" title="<@spring.message '_facet.by.language' /> ">
                   <noscript><h4><@spring.message '_facet.by.language' />:</h4></noscript>
                   <#assign columsize = 2>
                </#if>
                   <#break/>
            <#case "YEAR">
                <#if facet.links?size &gt; 0>
                    <#if facet.selected ><#assign togglerClass="toggler-c-opened"/></#if>
                   <div class="toggler-c ${togglerClass}>" title="<@spring.message '_facet.by.date' />">
                   <noscript><h4><@spring.message '_facet.by.date' />:</h4></noscript>
                   <#assign columsize = 2>
               </#if>
               <#break/>
            <#case "DCTYPE">
                <#if facet.links?size &gt; 0>
                <#if facet.selected ><#assign togglerClass="toggler-c-opened"/></#if>
               <div class="toggler-c ${togglerClass}" title="<@spring.message '_facet.by.type' />">
               <noscript><h4><@spring.message '_facet.by.type' />:</h4></noscript>
               <#assign columsize = 1>
               </#if>
               <#break/>
            <#case "TYPE">
                <#if facet.links?size &gt; 0>
                <#if facet.selected ><#assign togglerClass="toggler-c-opened"/></#if>
               <div class="toggler-c ${togglerClass}" title="<@spring.message '_facet.by.type' />">
               <noscript><h4><@spring.message '_facet.by.type' />:</h4></noscript>
               <#assign columsize = 1>
               </#if>
               <#break/>
            <#case "COLLECTION">
                <#if facet.links?size &gt; 0>
                   <#if facet.selected><#assign togglerClass="toggler-c-opened"/></#if>
                   <div class="toggler-c ${togglerClass} ui-icon-circle-triangle-s" title="<@spring.message '_facet.by.collection' />">
                   <noscript><h4><@spring.message '_facet.by.collection' />:</h4></noscript>
                   <#assign columsize = 1>
                </#if>
                   <#break/>
            <#case "PROVIDER">
                <#if facet.links?size &gt; 0>
                <#if facet.selected ><#assign togglerClass="toggler-c-opened"/></#if>
               <div class="toggler-c ${togglerClass}" title="<@spring.message '_facet.by.provider' />">
               <noscript><h4><@spring.message '_facet.by.provider' />:</h4></noscript>
               <#assign columsize = 1>
               </#if>
               <#break/>
            <#case "COUNTRY">
                <#if facet.links?size &gt; 0>
                <#if facet.selected ><#assign togglerClass="toggler-c-opened"/></#if>
               <div class="toggler-c ${togglerClass}" title="<@spring.message '_facet.by.country' />">
                   <noscript><h4><@spring.message '_facet.by.country' />:</h4></noscript>
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
        <h4><@spring.message '_header.actions'/>:</h4>
        <div class="related-links">
            <p class="linetop">
                <#if user??>
                    <a id="saveQuery" href="inc_facets_lists.ftl#" onclick="saveQuery('SavedSearch', '${queryToSave?url("utf-8")?js_string}', '${query?url("utf-8")?js_string}');"><@spring.message '_action.save.this.search'/></a>
                <#else>
                    <a href="inc_facets_lists.ftl#" onclick="highLight('mustlogin'); return false" class="disabled"><@spring.message '_action.save.this.search'/></a>
                </#if>
            </p>
            <div id="msg-save-search" class="msg-hide fg-pink"></div>
        </div>
    </#if>
</#compress>
