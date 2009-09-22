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
<#include "inc_header.ftl">

   <script type="text/javascript">
    $(document).ready(function() {
        $('div.toggler-c').toggleElements(
        { fxAnimation:'slide', fxSpeed:'fast', className:'toggler' });
        $('ul.toggler-c').toggleElements();
    });

    function removeRequest(className, id){
        $.ajax({
           type: "POST",
           url: "remove.ajax",
           data: "className="+className+"&id="+id,
           success: function(msg){
                window.location.reload();
           },
           error: function(msg) {
                alert("An error occured. The item could not be removed");
           }
         });
    };

     function showDefault(obj,iType){
        switch(iType)
        {
        case "TEXT":
          obj.src="images/item-page.gif";
          break;
        case "IMAGE":
          obj.src="images/item-image.gif";
          break;
        case "VIDEO":
          obj.src="images/item-video.gif";
          break;
        case "SOUND":
          obj.src="images/item-sound.gif";
          break;
        default:
          obj.src="images/item-page.gif";
        }
     }


   </script>

<body class="yui-skin-sam">

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
				<div class="yui-u first">

                     <table width="100%" class="tbl-list" summary="table with user information" id="tbl-user">
                        <tr><th><@spring.message 'UserInformation_t' /></th></tr>
                        <tr>
                            <td>
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

                                <#if user.role='ROLE_ADMINISTRATOR'>
                                <br/>
                                <p><a href="administration.html">Administrator Controls</a></p>
                                </#if>


                            </td>
                        </tr>
                    </table>


                    <table width="100%" class="tbl-list" summary="list with saved items" id="tbl-items">
                        <tr><th colspan="3"><@spring.message 'SavedItems_t'/></th></tr>
                        <#if user.savedItems?size &gt; 0>
                            <#list user.savedItems as item>
                                <#if item_index%2=0>
                                    <#assign bgclass = "bg-gray"/>
                                 <#else>
                                    <#assign bgclass = ""/>
                                 </#if>
                                <tr>
                                    <td valign="top" class="${bgclass} item-img"> ${item_index}
                                        <a href="full-doc.html?uri=${item.europeanaId.europeanaUri}">
                                         <#if useCache="true">
                                            <img class="thumb" align="middle" src="${cacheUrl}uri=${item.europeanaObject}&size=BRIEF_DOC" alt="Click for more information" height="50" style="float:left" onerror="showDefault(this,'${item.docType}')"/>
                                         <#else>
                                            <#if item.docType??>
                                              <img class="thumb" align="middle" src="${item.europeanaObject}" alt="Click for more information" height="50" style="float:left" onerror="showDefault(this,'${item.docType}')"/>
                                            <#else>
                                              <img class="thumb" align="middle" src="${item.europeanaObject}" alt="Click for more information" height="50" style="float:left" onerror="showDefault(this)"/>
                                            </#if>
                                         </#if>
                                        </a>
                                    </td>
                                    <td valign="top" class="${bgclass} item-info">
                                        <a href="full-doc.html?uri=${item.europeanaId.europeanaUri}"><strong><@stringLimiter "${item.title}" "50"/></strong></a>
                                        <p>
                                        creator: <em>${item.author}</em></p><p>
                                            <@spring.message 'DateSaved_t'/>: <em>${item.dateSaved?datetime}</em>
                                            </p>
                                    </td>
                                    <td class="${bgclass}" width="60"><button class="del-button" onclick="removeRequest('SavedItem',${item.id?string("0")});"><@spring.message 'Delete_t'/></button></td>
                                </tr>
                            </#list>

                        <#else/>
                            <tr><td><@spring.message 'NoSavedItems_t'/></td></tr>
                        </#if>
                    </table>

                    <div id="panelItems" class="panel" style="background: #ffffff; border: 4px solid black; height: 400px; display:none; position: absolute; top: 100px; left: 250px;">
                        <div class="bd">
                             <table width="100%" class="tbl-list" summary="list with saved items" id="tbl-items">
                                <tr><th colspan="3"><@spring.message 'SavedItems_t'/></th></tr>
                                    <#list user.savedItems as item>
                                        <tr>
                                            <td valign="top" class="item-img">
                                                <a href="full-doc.html?uri=${item.europeanaId.europeanaUri}">
                                                 <#if useCache="true">
                                                    <img class="thumb" align="middle" src="${cacheUrl}uri=${item.europeanaObject}&size=BRIEF_DOC" alt="Click for more information" height="50" style="float:left" onerror="showDefault(this,'${item.docType}')"/>
                                                 <#else>
                                                    <img class="thumb" align="middle" src="${item.europeanaObject}" alt="Click for more information" height="50" style="float:left" onerror="showDefault(this,'${item.docType}')"/>
                                                 </#if>
                                                </a>
                                            </td>
                                            <td valign="top" class="item-info">
                                                <a href="full-doc.html?uri=${item.europeanaId.europeanaUri}"><strong>${item.title}</strong></a>
                                                <p>creator: <em>${item.author}</em></p>
                                                <p><@spring.message 'DateSaved_t'/>: <em>${item.dateSaved?datetime}</em></p>
                                            </td>
                                            <td width="60"><button class="del-button" onclick="removeRequest('SavedItem',${item.id?string("0")});"><@spring.message 'Delete_t'/></button></td>
                                        </tr>
                                    </#list>
                            </table>
                        </div>

                    </div>

                </div>
				<div class="yui-u">
                    <table width="100%" class="tbl-list" summary="list with saved searches" id="tbl-searches">
                        <tr><th colspan="3"><@spring.message 'SavedSearches_t'/></th></tr>
                        <#if user.savedSearches?size &gt; 0>
                            <#list user.savedSearches as search>
                                <tr>
                                    <td valign="top" class="item-info">
                                        <a href="brief-doc.html?${search.query}">${search.query}</a>
                                        <p><@spring.message 'DateSaved_t'/>: <em>${search.dateSaved?datetime}</em></p>
                                    </td>
                                    <td width="60"><button class="del-button" onclick="removeRequest('SavedSearch',${search.id?string("0")});"><@spring.message 'Delete_t'/></button></td>
                                </tr>
                                <#if search_index &gt; 4>
                                      <tr><td colspan="2" align="right">

                                        <a href="#" id="showAllSearches"><@spring.message 'ShowAll_t'/></a>

                                    </td></tr>
                                    <#break/>
                                </#if>
                            </#list>
                        <#else/>
                            <tr><td><@spring.message 'NoSavedSearches_t'/></td></tr>
                        </#if>
                    </table>

                    <div id="panelSearches" class="panel">
                       <div class="bd">
                         <table width="100%" class="tbl-list" summary="list with saved searches" id="tbl-searches">
                            <tr><th colspan="3"><@spring.message 'SavedSearches_t'/></th></tr>
                                <#list user.savedSearches as search>
                                     <tr>
                                        <td valign="top" class="item-info">
                                            <a href="brief-doc.html?${search.query}">${search.query}</a>
                                            <p><@spring.message 'DateSaved_t'/>: <em>${search.dateSaved?datetime}</em></p>
                                        </td>
                                        <td width="60"><button class="del-button" onclick="removeRequest('SavedSearch',${search.id?string("0")});"><@spring.message 'Delete_t'/></button></td>
                                    </tr>
                                </#list>
                            </table>
                        </div>
                    </div>

                    <table width="100%" class="tbl-list" summary="list with saved searches" id="tbl-tags">

                        <tr><th colspan="3"><@spring.message 'SavedTags_t'/></th></tr>
                        <#if user.socialTags?size &gt; 0>

                            <#list user.socialTagLists as count>

                                <tr>
                                    <td valign="top" class="item-info" colspan="3">
                                        <#--<a href="brief-doc.html?query=europeana_userTag:${count.tag}"><strong>${count.tag} (${count.list?size})</strong></a>-->
                                        <a href="brief-doc.html?query=europeana_userTag:${count.tag}" onclick="toggleObject('tags_${count_index}')"><strong>${count.tag} (${count.list?size})</strong></a>
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
                                         <a href="brief-doc.html?query=europeana_userTag:${tag.tag}">${tag.title}</a><br/>
                                         <p><@spring.message 'DateSaved_t'/>: <em>${tag.dateSaved?datetime}</em></p>
                                     </td>
                                     <td width="60"><button class="del-button" onclick="removeRequest('SocialTag',${tag.id?string("0")});"><@spring.message 'Delete_t'/></button></td>
                                   </tr>
                                </#list>


                            </#list>
                        <#---->
                            <#--<#list user.socialTags as tag>-->
                                <#--<tr>-->
                                    <#--<td valign="top" class="item-info">-->
                                        <#--<a href="brief-doc.html?query=europeana_userTag:${tag.tag}"> <strong>${tag.tag}</strong></a>-->
                                        <#--<#if tag.europeanaObject??>-->
                                        <#--<img class="thumb" align="middle" src="${cacheUrl}uri=${tag.europeanaObject}&size=BRIEF_DOC" alt="Click for more information" height="50" style="float:left"/>-->
                                                                   <#--</#if>-->
                                        <#--<p><@spring.message 'DateSaved_t'/>: <em>${tag.dateSaved?datetime}</em></p>-->
                                    <#--</td>-->
                                    <#--<td width="60"><button class="del-button" onclick="removeRequest('SocialTag',${tag.id?string("0")});"><@spring.message 'Delete_t'/></button></td>-->
                                <#--</tr>-->
                                <#--<#if tag_index &gt; 4>-->
                                      <#--<tr><td colspan="2" align="right">-->

                                        <#--<a href="#" id="showAllTags">Show all saved tags</a>-->

                                    <#--</td></tr>-->
                                    <#--<#break/>-->
                                <#--</#if>-->
                            <#--</#list>-->
                        <#else>
                            <tr><td><@spring.message 'NoSavedTags_t'/></td></tr>
                        </#if>
                    </table>
                    <div id="panelTags" class="panel">
                       <div class="bd">
                         <table width="100%" class="tbl-list" summary="list with saved searches" id="tbl-tags">
                            <tr><th colspan="3"><@spring.message 'SavedTags_t'/></th></tr>
                              <#list user.socialTags as tag>
                                <tr>
                                    <td valign="top" class="item-info">
                                        <a href="brief-doc.html?query=europeana_userTag:${tag.tag}"> <strong>${tag.tag}</strong></a>
                                          <#if tag.europeanaObject??>
                                            <#if useCache="true">
                                                <img class="thumb" align="middle" src="${cacheUrl}uri=${tag.europeanaObject}&size=BRIEF_DOC" alt="Click for more information" height="50" style="float:left"/>
                                            </#if>
                                          </#if>
                                        <p><@spring.message 'DateSaved_t'/>: <em>${tag.dateSaved?datetime}</em></p>
                                    </td>
                                    <td width="60"><button class="del-button" onclick="removeRequest('SocialTag',${tag.id?string("0")});"><@spring.message 'Delete_t'/></button></td>
                                </tr>
                            </#list>
                            </table>
                        </div>
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
