<#assign thisPage = "mine.html">

<#compress>
    <#include "inc_header.ftl">
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

<div id="header">

    <div id="identity" class="grid_3">
        <h1>Delving</h1>
        <a href="/${portalName}/index.html" title="ABM"><img src="/${portalName}/${portalTheme}/images/abm-logo.jpg" alt="ABM"/></a>
    </div>

    <div class="grid_9">

        <div id="top-bar">
            <div class="inner">
            <@userbar/>
            </div>
        </div>

    </div>

</div>

<div id="main" class="grid_9">
    <div id="savedItems">
                    <ul>
                        <li><a href="#fragment-0" onclick="$.cookie('ui-tabs-3', '0', { expires: 1 });" title="<@spring.message '_mine.user.information' />"><span><@spring.message '_mine.user.information' /></span></a></li>
                        <li><a href="#fragment-1" onclick="$.cookie('ui-tabs-3', '1', { expires: 1 });" title="<@spring.message '_mine.saved.items'/>"><span><@spring.message '_mine.saved.items'/></span></a></li>
                        <li><a href="#fragment-2" onclick="$.cookie('ui-tabs-3', '2', { expires: 1 });" title="<@spring.message '_mine.saved.searches'/>"><span><@spring.message '_mine.saved.searches'/></span></a></li>
                        <li><a href="#fragment-3" onclick="$.cookie('ui-tabs-3', '3', { expires: 1 });" title="<@spring.message '_mine.saved.tags'/>"><span><@spring.message '_mine.saved.tags'/></span></a></li>
                    </ul>
                   <div id="fragment-0">
                      <table width="100%" class="tbl-list" summary="table with user information" id="tbl-user" >
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
                          <td><h4>Voornaam:</h4></td>
                          <td>${user.firstName}</td>
                        </tr>
                        </#if>
                        <#if user.lastName??>
                        <tr>
                          <td><h4>Achternaam:</h4></td>
                          <td>${user.lastName}</td>
                        </tr>
                        </#if>
                        <#if user.registrationDate??>
                        <tr>
                          <td><h4>Registratiedatum:</h4></td>
                          <td>${user.registrationDate?string("yyyy-MM-dd HH:mm:ss")}</td>
                        </tr>
                        </#if>
                        <#if user.lastLogin??>
                        <tr>
                          <td><h4>Laatste inlogdatum:</h4></td>
                          <td>${user.lastLogin?string("yyyy-MM-dd HH:mm:ss")}</td>
                        </tr>
                        </#if>
                    </table>
                   </div>
                    <div id="fragment-1">
                        <table width="95%" class="tbl-list" summary="list with saved items">
                            <#if user.items?size &gt; 0>
                                <#list user.items as item>
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
                                            creator: <em>${item.author}</em>
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
                                <tr><td><@spring.message '_mine.nosaveditems'/></td></tr>
                            </#if>
                        </table>
                    </div>
                   <div id="fragment-2">
                        <table width="100%" class="tbl-list" summary="list with saved searches">
                            <#if user.searches?size &gt; 0>
                                <#list user.searches as search>
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
                   <div id="fragment-3">
                    <table width="100%" class="tbl-list" summary="list with saved tags" id="tbl-tags">
                        <#if user.socialTags?size &gt; 0>
                            <#list user.socialTagLists as count>
                                <#assign tagQuery = "europeana_uri:("/>
                                <#list count.list as tag>
                                        <#-- todo: add tagQuery to href instead of searching for the userTag directly -->
                                       <#assign tagQuery = tagQuery + "+\"" + tag.europeanaUri + "\""/>
                                </#list>
                                <#assign tagQuery = tagQuery + ")"/>
                                <tr>
                                    <th valign="top" class="item-info" colspan="3">
                                        <#--${tagQuery}-->
                                        <#--<a href="/${portalName}/brief-doc.html?query=europeana_userTag:${count.tag}"><strong>${count.tag} (${count.list?size})</strong></a>-->
                                        <span class="ui-icon ui-icon-tag"></span><strong>${count.tag} (${count.list?size})</strong>
                                    </th>
                                </tr>
                                <#list count.list as tag>
                                   <tr>
                                     <td width="35" align="right">
                                         <#if tag.europeanaObject??>
                                            <a href="/${portalName}/record/${tag.europeanaUri}.html">
                                            <#if useCache = "true">
                                                <img class="thumb" src="${cacheUrl}uri=${tag.europeanaObject}&size=BRIEF_DOC" alt="Click for more information" width="25"/>
                                            </#if>

                                             <#if useCache="true">
                                                <img class="thumb" align="middle" src="${cacheUrl}uri=${tag.europeanaObject}&size=BRIEF_DOC" height="50"/>
                                             <#else>
                                                <#if tag.docType??>
                                                  <img class="thumb" align="middle" src="${tag.europeanaObject}" height="50" onerror="showDefault(this,'${tag.docType}')"/>
                                                <#else>
                                                  <img class="thumb" align="middle" src="${tag.europeanaObject}" height="50" onerror="showDefault(this)"/>
                                                </#if>
                                             </#if>
                                             </a>

                                         </#if>
                                     </td>
                                     <td valign="top" class="item-info">
                                         <a href="/${portalName}/record/${tag.europeanaUri}.html">${tag.title}</a><br/>
                                         <p><@spring.message '_mine.date.saved'/>: <em>${tag.dateSaved?datetime}</em></p>
                                     </td>
                                     <td width="60"><a href="#" class="fg-button ui-state-default fg-button-icon-left ui-corner-all" onclick="removeRequest('SocialTag',${tag.id?string("0")});"><span class="ui-icon ui-icon-trash"></span><@spring.message '_mine.delete'/></a></td>
                                   </tr>
                                </#list>


                            </#list>
                        <#else>
                            <tr><td><@spring.message '_mine.nosavedtags'/></td></tr>
                        </#if>
                    </table>

                </div>
            </div>
</div>

<div id="sidebar">
    <div id="search">
        <div class="inner">
        <@SearchForm "search_result"/>
        </div>
    </div>
</div>
    <#include "inc_footer.ftl"/>

</#compress>
