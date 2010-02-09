<#import "spring.ftl" as spring />
<#assign thisPage = "myeuropeana.html">
<#assign view = "table"/>
<#assign query = ""/>
<#assign cacheUrl = cacheUrl/>
<#if RequestParameters.view??>
<#assign view = "${RequestParameters.view}"/>
</#if>
<#if RequestParameters.query??>
<#assign query = "${RequestParameters.query}"/>
</#if>
<#assign user = user/>
<#compress>
<#include "inc_header.ftl">

<div id="doc4" class="yui-t2">
    <div id="hd">
        <#include "inc_top_nav.ftl"/>
    </div>
    <div id="bd">
        <div id="yui-main">
            <div class="yui-b">
                <div class="yui-g">
                    <h1>My Europeana</h1>
                </div>
                <div class="yui-g">
                    <div id="savedItems">
                        <ul>
                            <li><a href="#fragment-0"
                                   title="<@spring.message 'UserInformation_t' />"><span><@spring.message 'UserInformation_t' /></span></a>
                            </li>
                            <li><a href="#fragment-1"
                                   title="<@spring.message 'SavedItems_t'/>"><span><@spring.message 'SavedItems_t'/></span></a>
                            </li>
                            <li><a href="#fragment-2"
                                   title="<@spring.message 'SavedSearches_t'/>"><span><@spring.message 'SavedSearches_t'/></span></a>
                            </li>
                            <li><a href="#fragment-3"
                                   title="<@spring.message 'SavedTags_t'/>"><span><@spring.message 'SavedTags_t'/></span></a>
                            </li>
                        </ul>
                        <div id="fragment-0">
                            <table width="100%" class="tbl-list" summary="table with user information" id="tbl-user"
                                   height="300">
                                <tr>
                                    <td valign="top">
                                        <h4><@spring.message 'Username_t' />:</h4>
                                        <p>${user.userName}</p>
                                        <br/>

                                        <h4><@spring.message 'EmailAddress_t'/>:</h4>
                                        <p>${user.email}</p>

                                        <#if user.firstName??>
                                        First name: ${user.firstName}
                                        </#if>
                                        <#if user.lastName??>
                                        Last name: ${user.lastName}
                                        </#if>

                                    </td>
                                </tr>
                            </table>
                        </div>
                        <div id="fragment-1">
                            <table width="95%" class="tbl-list" summary="list with saved items">
                                <#if user.savedItems?size &gt; 0>
                                    <#assign imgtoshow = "item-image.gif"/>
                                    <#list user.savedItems as item>
                                    <#switch item.docType>
                                        <#case "IMAGE">
                                            <#assign imgtoshow = "item-image.gif"/>
                                        <#break>
                                        <#case "VIDEO">
                                            <#assign imgtoshow = "item-video.gif"/>
                                        <#break>
                                        <#case "SOUND">
                                            <#assign imgtoshow = "item-sound.gif"/>
                                        <#break>
                                        <#case "TEXT">
                                            <#assign imgtoshow = "item-page.gif"/>
                                        <#break>
                                    </#switch>
                                    <tr>
                                        <td valign="top" class="item-img">
                                            <#if !item.europeanaId.orphan>
                                            <a href="full-doc.html?uri=${item.europeanaId.europeanaUri}">
                                                <#if useCache="true">
                                                    <img class="thumb" align="middle"
                                                     src="${cacheUrl}uri=${item.europeanaObject}&size=BRIEF_DOC"
                                                     alt="Click for more information" height="50" style="float:left"
                                                     onerror="showDefault(this,'${item.docType}')"/>
                                                </#if>
                                            </a>
                                            <#else>
                                                <img class="thumb" src="images/${imgtoshow}" width="35"/>
                                            </#if>
                                        </td>
                                        <td valign="top" class="item-info">
                                           <#if !item.europeanaId.orphan>
                                                <a href="full-doc.html?uri=${item.europeanaId.europeanaUri}"><strong><@stringLimiter "${item.title}" "50"/></strong></a>
                                                <p>
                                                    creator: <em>${item.author}</em></p>
                                                <p>
                                                    <@spring.message 'DateSaved_t'/>: <em>${item.dateSaved?datetime}</em>
                                                </p>
                                            <#else>
                                                <strong class="fg-gray" ><@stringLimiter "${item.title}" "50"/></strong>
                                                <div class="ui-widget ui-info">
                                                    <strong>Attention:</strong> this item has been withdrawn by the provider.
                                                </div>


                                            </#if>

                                            <div class="ui-widget ui-error" id="removeRequestMessage-${item.id?string("0")}" style="display:none;">
                                                <p><strong>An error occured.</strong> The item could not be removed</p>
                                            </div>

                                        </td>
                                        <td width="220" align="right">
                                       <!-- AddThis Button BEGIN -->
                                        <#assign  showthislang = locale>
                                        <#if  locale = "mt" || locale = "et">
                                           <#assign  showthislang = "en">
                                        </#if>
                                           <a class="addthis_button"
                                              href="http://www.addthis.com/bookmark.php?v=250&amp;username=xa-4b4f08de468caf36"
                                              addthis:url="${item.europeanaId.europeanaUri}"
                                               <#if !item.europeanaId.orphan>
                                                    addthis:title="<@stringLimiter '${item.title}' '50'/>"
                                               <#else>
                                                    addthis:title="Europeana Object"
                                               </#if>
                                             >
                                             <img src="http://s7.addthis.com/static/btn/lg-share-${showthislang}.gif" alt="Bookmark and Share" style="border:0"/></a>
                                            <script type="text/javascript" src="http://s7.addthis.com/js/250/addthis_widget.js#username=xa-4b4f08de468caf36"></script>
                                            <script type="text/javascript">
                                                var addthis_config = {
                                                     ui_language: "${showthislang}"
                                                }
                                              </script>
                                           <!-- AddThis Button END -->
                                            <br /><br />
                                               <!-- AddThis Button END -->
                                            <button class="del-button"
                                                    onclick="removeRequest('SavedItem',${item.id?string("0")});"><@spring.message 'Delete_t'/></button>

                                            <#if user?? && !item.europeanaId.orphan>
                                                <#if user.role == "ROLE_CARROUSEL" || user.role == "ROLE_EDITOR">
                                                    <#if item.hasCarouselItem()>
                                                            <button class="del-button"
                                                                 onclick="removeRequest('CarouselItem',${item.id?string("0")});">Carousel - <@spring.message 'Delete_t'/></button>
                                                    <#else> <#-- if !item.europeanaId.hasCarouselItem() -->  <#-- check if nobody else has saved this item -->
                                                         <button class="del-button"
                                                             onclick="addEditorItemRequest('CarouselItem',${item.id?string("0")});">Carousel - <@spring.message 'Add_t'/></button>
                                                    </#if>
                                                </#if>
                                            </#if>
                                        </td>
                                    </tr>
                                    </#list>

                                    <#else>
                                    <tr>
                                        <td><@spring.message 'NoSavedItems_t'/></td>
                                    </tr>
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
                                        <div class="ui-widget ui-error" id="removeRequestMessage-${search.id?string("0")}" style="display:none">
                                            <p><strong>An error occured.</strong> The item could not be removed</p>
                                        </div>
                                    </td>
                                    <td width="230" align="right">
                                       <button class="del-button"
                                                onclick="removeRequest('SavedSearch',${search.id?string("0")});"><@spring.message 'Delete_t'/></button>
                                        <#if user?? >
                                            <#if user.role == "ROLE_PACTA" || user.role == "ROLE_EDITOR">
                                            <#-- todo: move search term conditional in here -->
                                                <#if search.hasSearchTerm()>
                                                    <button class="del-button"
                                                        onclick="removeRequest('SearchTerm',${search.id?string("0")});">SearchTerm <@spring.message 'Delete_t'/></button>
                                                <#else>
                                                    <#-- todo: add add button -->
                                                    <button class="del-button"
                                                        onclick="addEditorItemRequest('SearchTerm',${search.id?string("0")});">SearchTerm - <@spring.message 'Add_t'/></button>
                                                </#if>
                                            </#if>
                                        </#if>
                                        </td>
                                </tr>
                                </#list>
                                <#else>
                                <tr>
                                    <td><@spring.message 'NoSavedSearches_t'/></td>
                                </tr>
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
                                    <td valign="top" class="item-info" colspan="3">
                                        <#--${tagQuery}-->
                                        <#--<a href="brief-doc.html?query=europeana_userTag:${count.tag}"><strong>${count.tag} (${count.list?size})</strong></a>-->
                                           <strong>${count.tag} (${count.list?size})</strong>
                                    </td>
                                </tr>
                                <#list count.list as tag>
                                <tr>
                                    <td width="35" align="right">
                                          <#if !tag.europeanaId.orphan>
                                            <#if tag.europeanaObject??>
                                            <#if useCache = "true">
                                                <img class="thumb" src="${cacheUrl}uri=${tag.europeanaObject}&size=BRIEF_DOC"
                                                 alt="Click for more information" width="25" onerror="showDefault(this,'${tag.docType}')"/>
                                            </#if>
                                             </#if>
                                        <#else>
                                            <img class="thumb" src="::"
                                                 alt="Click for more information" width="25" onerror="showDefault(this,'${tag.docType}')"/>
                                        </#if>
                                    </td>
                                    <td valign="top" class="item-info">
                                        <#if !tag.europeanaId.orphan>
                                            <a href="full-doc.html?uri=${tag.europeanaUri}">${tag.title}</a><br/>
                                            <p><@spring.message 'DateSaved_t'/>: <em>${tag.dateSaved?datetime}</em></p>
                                        <#else>
                                            <strong class="fg-gray">${tag.title}</strong>
                                                 <div class="ui-widget ui-info">
                                                    <strong>Attention:</strong> this item has been withdrawn by the provider.
                                                </div>
                                        </#if>
                                        <div class="ui-widget ui-error" id="removeRequestMessage-${tag.id?string("0")}" style="display:none">
                                            <p><strong>An error occured.</strong> The item could not be removed</p>
                                        </div>
                                    </td>
                                    <td width="60">
                                        <button class="del-button"
                                                onclick="removeRequest('SocialTag',${tag.id?string("0")});"><@spring.message 'Delete_t'/></button>
                                    </td>
                                </tr>
                                </#list>


                                </#list>
                                <#else>
                                <tr>
                                    <td><@spring.message 'NoSavedTags_t'/></td>
                                </tr>
                                </#if>
                            </table>

                        </div>
                    </div>
                </div>

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
</#compress>