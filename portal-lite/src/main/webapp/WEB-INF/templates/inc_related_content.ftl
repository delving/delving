        <h5><@spring.message 'RelatedContent_t' />:</h5>



                <table summary="related items" id="tbl-related-items" width="100%">
                    <#assign max=3/><!-- max shown in list -->
                    <#list result.relatedItems as doc>
                    <#if doc_index &gt; 2><#break/></#if>
                    <tr>
                        <td width="45" valign="top">
                            <div class="related-thumb-container">
                                <#if queryStringForPaging??>
                                    <a href="full-doc.html?${queryStringForPaging?html}&amp;start=${doc.index?c}&amp;uri=${doc.id}&amp;view=${view}&amp;startPage=1&amp;pageId=brd&amp;tab=">
                                 <#else>
                                    <a href="full-doc.html?uri=${doc.id}">
                                 </#if>
                                 <#if useCache="true">
                                    <img src="${cacheUrl}uri=${doc.thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${doc.type}&amp;view=${view}" alt="Click here to view related item" width="40"/>
                                <#else>
                                    <img src="${doc.thumbnail}" alt="Click here to view related item" width="40" onerror="showDefault(this,'${doc.type}')"/>
                                </#if>

                                    </a>
                            </div>
                        </td>

                        <td class="item-titles" valign="top" width="130">
                            <#if queryStringForPaging??>
                            <a href="full-doc.html?${queryStringForPaging?html}&amp;start=${doc.index?c}&amp;uri=${doc.id}&amp;startPage=1&amp;pageId=brd"><@stringLimiter "${doc.title}" "50"/></a>
                            <#else>
                            <a href="full-doc.html?uri=${doc.id}"><@stringLimiter "${doc.title}" "50"/></a>
                            </#if>
                        </td>
                    </tr>

                    </#list>
                    <#if result.relatedItems?size &gt; max>
                    <tr>
                        <td id="see-all" colspan="2"><a href='brief-doc.html?query=europeana_uri:"${uri}"&amp;view=${view}'><@spring.message 'SeeAllRelatedItems_t' /></a></td>
                    </tr>
                    </#if>
                </table>

            <#-- todo: ReImplement this after good solution wrt managing content of UserTags is found -->
            <#--<div class="toggler-c"title="<@spring.message 'UserTags_t' />">-->
                <#--<p>-->
                    <#--<#list model.fullDoc.europeanaUserTag as userTag>-->
                    <#--<a href="brief-doc.html?query=europeana_userTag:${userTag}&view=${view}">${userTag}</a><br/>-->
                    <#--</#list>-->
                <#--</p>-->
            <#--</div>-->

            <h5><@spring.message 'Actions_t' />:</h5>
            <#if user??>


                <p class="linetop">
                    <a href="#" onclick="saveItem('SavedItem','${postTitle?js_string}','${postAuthor?js_string}','${result.fullDoc.id?js_string}','${result.fullDoc.thumbnails[0]?js_string}','${result.fullDoc.europeanaType}');"><@spring.message 'SaveToMyEuropeana_t' /></a>
                    <div id="msg-save-item" class="msg-hide"></div>
                </p>

            <h6><@spring.message 'AddATag_t' /></h6>

                <#--<div id="ysearchautocomplete">-->
                      <form action="#" method="post" onsubmit="addTag('SocialTag', document.getElementById('tag').value,'${result.fullDoc.id}','${result.fullDoc.thumbnails[0]?js_string}','${postTitle}','${result.fullDoc.europeanaType}'); return false;"  id="form-addtag" name="form-addtag" accept-charset="UTF-8">
                        <input type="text" name="tag" id="tag" maxlength="50" class="text"/>
                        <input type="submit" class="button" value="Add"/>
                    </form>
                    <div id="msg-save-tag" class="hide"></div>
                <#--</div>-->

                <h6><@spring.message 'ShareWithAFriend_t' /></h6>
                <form action="#" method="post" onsubmit='sendEmail("${result.fullDoc.id}"); return false;' id="form-sendtoafriend" accept-charset="UTF-8">
                    <label for="friendEmail"></label>
                    <input type="text" name="friendEmail" class="required email text" id="friendEmail" maxlength="50" value="<@spring.message 'EmailAddress_t' />"
                           onfocus="this.value=''"/>
                    <input type="submit" id="mailer" class="button" value="<@spring.message 'Send_t' />"/>

                </form>
                <div id="msg-send-email" class="hide"></div>


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