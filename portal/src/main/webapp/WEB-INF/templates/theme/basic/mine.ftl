<#assign thisPage = "mine.html">

<#compress>


<#include "includeMarcos.ftl">

<@addHeader "${portalDisplayName}", "",["mine.js"],[]/>


<style>
    .ui-tabs .ui-tabs-hide {
        display: none;
    }
</style>
<script type="text/javascript">

    $(function() {
        $("#savedItems").tabs('selected', 0);
    });

</script>

<section id="main" class="grid_12">

    <div id="savedItems">
        <ul>
            <li><a href="#tab-0" onclick="$.cookie('ui-tabs-3', '0', { expires: 1 });" title="<@spring.message '_mine.user.information' />"><span><@spring.message '_mine.user.information' /></span></a></li>
            <li><a href="#tab-1" onclick="$.cookie('ui-tabs-3', '1', { expires: 1 });" title="<@spring.message '_mine.saved.items'/>"><span><@spring.message '_mine.saved.items'/></span></a></li>
            <li><a href="#tab-2" onclick="$.cookie('ui-tabs-3', '2', { expires: 1 });" title="<@spring.message '_mine.saved.searches'/>"><span><@spring.message '_mine.saved.searches'/></span></a></li>
            <#--<li><a href="#tab-3" onclick="$.cookie('ui-tabs-3', '3', { expires: 1 });" title="<@spring.message '_mine.saved.tags'/>"><span><@spring.message '_mine.saved.tags'/></span></a></li>-->
        </ul>
       <div id="tab-0">
          <table class="tbl-list" summary="table with user information" id="tbl-user" >
              <caption><@spring.message '_mine.user.information' /></caption>
            <tr>
                <td width="150">
                    <h4><@spring.message '_mine.username' />:</h4>
                </td>
                <td>${user.userName}</td>
            </tr>
            <tr>
              <td><h4><@spring.message '_mine.email.address'/>:<h/4></td>
              <td>${user.email}</td>
            </tr>
            <#if user.firstName??>
            <tr>
              <td><h4><@spring.message '_mine.firstname'/>:</h4></td>
              <td>${user.firstName}</td>
            </tr>
            </#if>
            <#if user.lastName??>
            <tr>
              <td><h4><@spring.message '_mine.last.name'/>:</h4></td>
              <td>${user.lastName}</td>
            </tr>
            </#if>
            <#if user.registrationDate??>
            <tr>
              <td><h4><@spring.message '_mine.user.registration.date'/>:</h4></td>
              <td>${user.registrationDate}</td>
            </tr>
            </#if>
            <#if user.lastLogin??>
            <tr>
              <td><h4><@spring.message '_mine.user.last.login.date'/>:</h4></td>
              <td>${user.lastLogin?string("yyyy-MM-dd HH:mm:ss")}</td>
            </tr>
            </#if>
        </table>
       </div>
        <div id="tab-1">
            <table class="tbl-list" summary="list with saved items">
                <caption><@spring.message '_mine.saved.items'/></caption>
                <#if user.savedItems?size &gt; 0>
                    <#list user.savedItems as item>
                        <tr>
                            <td valign="top" class="item-img">
                                <a href="/${portalName}/record/${item.europeanaId.europeanaUri}.html">
                                 <#if useCache="true">
                                    <img class="thumb" align="middle" src="${cacheUrl}uri=${item.europeanaObject}&size=BRIEF_DOC" alt="Click for more information" height="50" style="float:left" onerror="showDefaultSmall(this,'${item.docType}')"/>
                                 <#else>
                                    <#if item.docType??>
                                      <img class="thumb" align="middle" src="${item.europeanaObject}" alt="Click for more information" height="50" style="float:left" onerror="showDefault(this,'${item.docType}')"/>
                                    <#else>
                                      <img class="thumb" align="middle" src="${item.europeanaObject}" alt="Click for more information" height="50" style="float:left" onerror="showDefault(this)"/>
                                    </#if>
                                 </#if>
                                </a>
                            </td>
                            <td valign="top" class="item-info">
                                <a href="/${portalName}/record/${item.europeanaId.europeanaUri}.html">
                                    <strong><@stringLimiter "${item.title}" "50"/></strong>
                                </a>
                                <br/>
                                <@spring.message '_search.field.creator'/>: <em>${item.author}</em>
                                <br/>
                                <@spring.message '_mine.date.saved'/>: <em>${item.dateSaved?datetime}</em>
                                <br/><br/>
                            </td>
                            <td width="60">
                                <a href="#" class="fg-button ui-state-default fg-button-icon-left ui-corner-all" onclick="removeRequest('SavedItem',${item.id?string("0")});"><span class="ui-icon ui-icon-trash"></span><@spring.message '_mine.delete'/></a>
                            </td>
                        </tr>
                    </#list>

                <#else>
                    <tr><td><@spring.message '_mine.nosaved.items'/></td></tr>
                </#if>
            </table>
        </div>
       <div id="tab-2">
            <table class="tbl-list" summary="list with saved searches">
                <caption><@spring.message '_mine.saved.searches'/></caption>
                <#if user.savedSearches?size &gt; 0>
                    <#list user.savedSearches as search>
                        <tr>
                            <td width="5"><a href="/${portalName}/brief-doc.html?${search.query}"><span class="ui-icon ui-icon-search"></span></a></td>
                            <td valign="top" class="item-info">
                                <a href="/${portalName}/brief-doc.html?${search.query}">${search.queryString}</a>
                                <p><@spring.message '_mine.date.saved'/>: <em>${search.dateSaved?datetime}</em></p>
                            </td>
                            <td width="60"><a href="#" class="fg-button ui-state-default fg-button-icon-left ui-corner-all" onclick="removeRequest('SavedSearch',${search.id?string("0")});"><span class="ui-icon ui-icon-trash"></span><@spring.message '_mine.delete'/></a></td>
                        </tr>
                    </#list>
                <#else>
                    <tr><td><@spring.message '_mine.nosavedsearches'/></td></tr>
                </#if>
            </table>
        </div>
       <#--<div id="tab-3">-->
        <#--<table class="tbl-list" summary="list with saved tags" id="tbl-tags">-->
            <#--<caption><@spring.message '_mine.saved.tags'/></caption>-->
            <#--<#if user.socialTags?size &gt; 0>-->
                <#--<#list user.socialTagLists as count>-->
                    <#--<#assign tagQuery = "europeana_uri:("/>-->
                    <#--<#list count.list as tag>-->
                            <#--&lt;#&ndash; todo: add tagQuery to href instead of searching for the userTag directly &ndash;&gt;-->
                           <#--<#assign tagQuery = tagQuery + "+\"" + tag.europeanaUri + "\""/>-->
                    <#--</#list>-->
                    <#--<#assign tagQuery = tagQuery + ")"/>-->
                    <#--<tr>-->
                        <#--<th valign="top" class="item-info" colspan="3">-->
                            <#--&lt;#&ndash;${tagQuery}&ndash;&gt;-->
                            <#--&lt;#&ndash;<a href="/${portalName}/brief-doc.html?query=europeana_userTag:${count.tag}"><strong>${count.tag} (${count.list?size})</strong></a>&ndash;&gt;-->
                            <#--<span class="ui-icon ui-icon-tag"></span><strong>${count.tag} (${count.list?size})</strong>-->
                        <#--</th>-->
                    <#--</tr>-->
                    <#--<#list count.list as tag>-->
                       <#--<tr>-->
                         <#--<td width="35" align="right">-->
                             <#--<#if tag.europeanaObject??>-->
                                <#--<a href="/${portalName}/record/${tag.europeanaUri}.html">-->
                                <#--<#if useCache = "true">-->
                                    <#--<img class="thumb" src="${cacheUrl}uri=${tag.europeanaObject}&size=BRIEF_DOC" alt="Click for more information" width="25"/>-->
                                <#--</#if>-->

                                 <#--<#if useCache="true">-->
                                    <#--<img class="thumb" align="middle" src="${cacheUrl}uri=${tag.europeanaObject}&size=BRIEF_DOC" height="50"/>-->
                                 <#--<#else>-->
                                    <#--<#if tag.docType??>-->
                                      <#--<img class="thumb" align="middle" src="${tag.europeanaObject}" height="50" onerror="showDefault(this,'${tag.docType}')"/>-->
                                    <#--<#else>-->
                                      <#--<img class="thumb" align="middle" src="${tag.europeanaObject}" height="50" onerror="showDefault(this)"/>-->
                                    <#--</#if>-->
                                 <#--</#if>-->
                                 <#--</a>-->

                             <#--</#if>-->
                         <#--</td>-->
                         <#--<td valign="top" class="item-info">-->
                             <#--<a href="/${portalName}/record/${tag.europeanaUri}.html">${tag.title}</a><br/>-->
                             <#--<p><@spring.message '_mine.date.saved'/>: <em>${tag.dateSaved?datetime}</em></p>-->
                         <#--</td>-->
                         <#--<td width="60"><a href="#" class="fg-button ui-state-default fg-button-icon-left ui-corner-all" onclick="removeRequest('SocialTag',${tag.id?string("0")});"><span class="ui-icon ui-icon-trash"></span><@spring.message '_mine.delete'/></a></td>-->
                       <#--</tr>-->
                    <#--</#list>-->


                <#--</#list>-->
            <#--<#else>-->
                <#--<tr><td><@spring.message '_mine.nosavedtags'/></td></tr>-->
            <#--</#if>-->
        <#--</table>-->

    <#--</div>-->
            </div>
</section>


    <@addFooter/>

</#compress>
