<#assign thisPage = "myeuropeana.html">

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

<div id="sidebar" class="grid_3">

    <div id="identity">
            <h1>Europeana Lite</h1>
            <a href="index.html" title="Europeana lite"><img src="images/europeana_open_logo_small.jpg" alt="European Open Source"/></a>
    </div>

</div>

<div id="main" class="grid_9">

    <div id="top-bar">
        <@userbar/>
        <#include "language_select.ftl">
    </div>

    <div class="clear"></div>

    <div id="search">
            <@SearchForm "search_result"/>
    </div>

    <div class="clear"></div>

    <div id="myeuropeana">
            <h1>My Europeana</h1>

               <div id="savedItems">
                    <ul>
                        <li><a href="#fragment-0" title="<@spring.message 'UserInformation_t' />"><span><@spring.message 'UserInformation_t' /></span></a></li>
                        <li><a href="#fragment-1" title="<@spring.message 'SavedItems_t'/>"><span><@spring.message 'SavedItems_t'/></span></a></li>
                        <li><a href="#fragment-2" title="<@spring.message 'SavedSearches_t'/>"><span><@spring.message 'SavedSearches_t'/></span></a></li>
                        <li><a href="#fragment-3" title="<@spring.message 'SavedTags_t'/>"><span><@spring.message 'SavedTags_t'/></span></a></li>
                    </ul>
                   <div id="fragment-0">
                      <table width="100%" class="tbl-list" summary="table with user information" id="tbl-user" height="300">
                        <tr>
                            <td valign="top">
                                <h4><@spring.message 'Username_t' />:</h4>
                                <p>${user.userName}</p>
                                <br />

                                <h4><@spring.message 'EmailAddress_t'/>:</h4>
                                <p>${user.email}</p>

                            <#if user.firstName?exists>
                                First name: ${user.firstName}
                            </#if>
                            <#if user.lastName?exists>
                                Last name: ${user.lastName}
                            </#if>

                            </td>
                        </tr>
                    </table>
                   </div>
                    <div id="fragment-1">
                        <table width="95%" class="tbl-list" summary="list with saved items">
                            <#if user.savedItems?size &gt; 0>
                                <#list user.savedItems as item>
                                    <tr>
                                        <td valign="top" class="item-img">
                                            <a href="full-doc.html?uri=${item.europeanaId.europeanaUri}">
                                             <#if useCache="true">
                                                <img class="thumb" align="middle" src="${cacheUrl}uri=${item.europeanaObject}&size=BRIEF_DOC" alt="Click for more information" height="50" style="float:left" onerror="showDefaultSmall(this,'${item.docType}')"/>
                                             <#--<#else>-->
                                                <#--<#if item.docType??>-->
                                                  <#--<img class="thumb" align="middle" src="${item.europeanaObject}" alt="Click for more information" height="50" style="float:left" onerror="showDefault(this,'${item.docType}')"/>-->
                                                <#--<#else>-->
                                                  <#--<img class="thumb" align="middle" src="${item.europeanaObject}" alt="Click for more information" height="50" style="float:left" onerror="showDefault(this)"/>-->
                                                <#--</#if>-->
                                             </#if>
                                            </a>
                                        </td>
                                        <td valign="top" class="item-info">
                                            <a href="full-doc.html?uri=${item.europeanaId.europeanaUri}"><strong><@stringLimiter "${item.title}" "50"/></strong></a>
                                            <p>
                                            creator: <em>${item.author}</em></p><p>
                                                <@spring.message 'DateSaved_t'/>: <em>${item.dateSaved?datetime}</em>
                                                </p>
                                        </td>
                                        <td width="60"><button  onclick="removeRequest('SavedItem',${item.id?string("0")});"><@spring.message 'Delete_t'/></button></td>
                                    </tr>
                                </#list>

                            <#else/>
                                <tr><td><@spring.message 'NoSavedItems_t'/></td></tr>
                            </#if>
                        </table>
                    </div>
                   <div id="fragment-2">
                        <table width="100%" class="tbl-list" summary="list with saved searches">
                            <#if user.savedSearches?size &gt; 0>
                                <#list user.savedSearches as search>
                                    <tr>
                                        <td valign="top" class="item-info">
                                            <a href="brief-doc.html?${search.query}">${search.queryString}</a>
                                            <p><@spring.message 'DateSaved_t'/>: <em>${search.dateSaved?datetime}</em></p>
                                        </td>
                                        <td width="60"><button onclick="removeRequest('SavedSearch',${search.id?string("0")});"><@spring.message 'Delete_t'/></button></td>
                                    </tr>
                                </#list>
                            <#else/>
                                <tr><td><@spring.message 'NoSavedSearches_t'/></td></tr>
                            </#if>
                        </table>
                    </div>
                   <div id="fragment-3">
                    <table width="100%" class="tbl-list" summary="list with saved searches" id="tbl-tags">
                        <#if user.socialTags?size &gt; 0>
                            <#list user.socialTagLists as count>
                                <#assign tagQuery = "europeana_uri:("/>
                                <#list count.list as tag>
                                        <#-- todo: add tagQuery to href instead of searching for the userTag directly -->
                                       <#assign tagQuery = tagQuery + "+\"" + tag.europeanaUri + "\""/>
                                </#list>
                                <#assign tagQuery = tagQuery + ")"/>
                                <tr>
                                    <td valign="top" class="item-info" colspan="3">
                                        <#--${tagQuery}-->
                                        <#--<a href="brief-doc.html?query=europeana_userTag:${count.tag}"><strong>${count.tag} (${count.list?size})</strong></a>-->
                                        <strong>${count.tag} (${count.list?size})</strong>
                                    </td>
                                </tr>
                                <#list count.list as tag>
                                   <tr>
                                     <td width="35" align="right">
                                         <#if tag.europeanaObject??>
                                            <#if useCache = "true">
                                                <img class="thumb" src="${cacheUrl}uri=${tag.europeanaObject}&size=BRIEF_DOC" alt="Click for more information" width="25"/>
                                            </#if>
                                         </#if>
                                     </td>
                                     <td valign="top" class="item-info">
                                         <a href="full-doc.html?uri=${tag.europeanaUri}">${tag.title}</a><br/>
                                         <p><@spring.message 'DateSaved_t'/>: <em>${tag.dateSaved?datetime}</em></p>
                                     </td>
                                     <td width="60"><button onclick="removeRequest('SocialTag',${tag.id?string("0")});"><@spring.message 'Delete_t'/></button></td>
                                   </tr>
                                </#list>


                            </#list>
                        <#else>
                            <tr><td><@spring.message 'NoSavedTags_t'/></td></tr>
                        </#if>
                    </table>

                </div>
            </div>
</div>
	    <#include "inc_footer.ftl"/>

</#compress>