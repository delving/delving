

        <h6><@spring.message 'RelatedContent_t' />:</h6>

                <table summary="related items" id="tbl-related-items" width="100%">
                    <#assign max=3/><!-- max shown in list -->
                    <#list result.relatedItems as doc>
                        <#if doc_index &gt; 2><#break/></#if>
                         <#-- empty image checker -->
                        <#if doc.thumbnail = " ">
                            <#assign thumbnail = "noImageFound"/>
                        <#else>
                            <#assign thumbnail = "${doc.thumbnail}"/>
                        </#if>
                        <tr>
                            <td width="35" valign="top">
                                <div class="related-thumb-container">
                                    <#if queryStringForPaging??>
                                        <a href="full-doc.html?${queryStringForPaging?html}&amp;start=${doc.index?c}&amp;uri=${doc.id}&amp;view=${view}&amp;startPage=1&amp;pageId=brd&amp;tab=">
                                     <#else>
                                        <a href="full-doc.html?uri=${doc.id}">
                                     </#if>
                                     <#if useCache="true">
                                        <img src="${cacheUrl}uri=${thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${doc.type}&amp;view=${view}" alt="Click here to view related item" width="35" onerror="showDefault(this,'${doc.type}',this.src)"/>
                                    <#else>
                                        <img src="${thumbnail}" alt="Click here to view related item" width="35" onerror="showDefaultSmall(this,'${doc.type}',this.src)"/>
                                    </#if>

                                        </a>
                                </div>
                            </td>

                            <td class="item-titles" valign="top" width="300">
                                <#if queryStringForPaging??>
                                <a href="full-doc.html?${queryStringForPaging?html}&amp;start=${doc.index?c}&amp;uri=${doc.id}&amp;startPage=1&amp;pageId=brd"><@stringLimiter "${doc.title}" "50"/></a>
                                <#else>
                                <a href="full-doc.html?uri=${doc.id}"><@stringLimiter "${doc.title}" "50"/></a>
                                </#if>
                            </td>
                        </tr>
                    </#list>
                </table>

                <#if result.relatedItems?size &gt; max>
                <p>
                    <a href='brief-doc.html?query=europeana_uri:"${uri}"&amp;view=${view}'>
                    <@spring.message 'SeeAllRelatedItems_t' /></a>
                </p>
                </#if>

                <div class="clear"></div>


                <#--<h3><@spring.message 'Actions_t' />:</h3>-->

                <#if user??>

                <h6><@spring.message 'ShareWithAFriend_t' /></h6>

                <form action="#" method="post" onsubmit='sendEmail("${result.fullDoc.id}"); return false;' id="form-sendtoafriend" accept-charset="UTF-8">
                    <label for="friendEmail"></label>
                    <input type="text" name="friendEmail" class="required email text" id="friendEmail" maxlength="50" value="<@spring.message 'EmailAddress_t' />"
                           onfocus="this.value=''"/>
                    <input type="submit" id="mailer" class="button" value="<@spring.message 'Send_t' />"/>

                </form>

                <span id="msg-send-email" class="hide"></span>

                <h6><@spring.message 'AddATag_t' /></h6>

                <form action="#" method="post" onsubmit="addTag('SocialTag', document.getElementById('tag').value,'${result.fullDoc.id}','${result.fullDoc.thumbnails[0]?js_string}','${postTitle}','${result.fullDoc.europeanaType}'); return false;"  id="form-addtag" name="form-addtag" accept-charset="UTF-8">
                          <input type="text" name="tag" id="tag" maxlength="50" class="text"/>
                          <input type="submit" class="button tag" value="<@spring.message 'Add_t' />"/>
                      </form>
                <span id="msg-save-tag" class="hide"></span>


                <h6><@spring.message 'UserTags_t' /></h6>

                <#-- todo: ReImplement this after good solution wrt managing content of UserTags is found -->
                <div class="toggler-c ui-icon-circle-triangle-s"title="<@spring.message 'UserTags_t' />">
                    <#--<#assign usertags = ["Manuscript","Treasure","Religion"]/>-->
                    <p>
                        <#list socialTags as userTag>
                            <a href="brief-doc.html?query=europeana_userTag:${userTag}&view=${view}">${userTag}</a><br/>
                        </#list>
                    </p>
                    <#--<#list usertags as tag>-->
                        <#--${tag}<br/>-->
                    <#--</#list>-->
                </div>

                <#--<p>-->
                    <#--<a href="#"  class="fg-button ui-state-default fg-button-icon-left ui-corner-all" onclick="saveItem('SavedItem','${postTitle?js_string}','${postAuthor?js_string}','${result.fullDoc.id?js_string}','${result.fullDoc.thumbnails[0]?js_string}','${result.fullDoc.europeanaType}');">-->
                        <#--<span class="ui-icon ui-icon-disk"></span><@spring.message 'SaveToMyEuropeana_t' />-->
                    <#--</a>-->

                    <#--<div id="msg-save-item" class="msg-hide"></div>-->

                <#--</p>-->

                    <#if result.fullDoc.europeanaType == "IMAGE">
                        <#if result.fullDoc.europeanaIsShownBy[0]?? && imageAnnotationToolBaseUrl?? && imageAnnotationToolBaseUrl!="">
                            <p class="linetop">
                                <a href="${imageAnnotationToolBaseUrl}?user=${user.userName}&objectURL=${result.fullDoc.europeanaIsShownBy[0]}&id=${result.fullDoc.id}" target="_blank"><@spring.message 'AddAnnotation_t' /></a>
                            </p>
                        </#if>
                    </#if>
                <div class="clear"></div>

        <#else>
            <div class="related-links">
                <p>
                    <a  href="login.html" class="disabled" onclick="highLight('mustlogin'); return false;"><@spring.message 'AddATag_t' /></a>
                </p>
                <p>
                    <a  href="login.html" class="disabled" onclick="highLight('mustlogin'); return false;"><@spring.message 'ShareWithAFriend_t' /></a>
                </p>
                <p>
                    <a  href="login.html" class="disabled" onclick="highLight('mustlogin'); return false;"><@spring.message 'SaveToMyEuropeana_t' /></a>
                </p>
            </div>

        </#if>