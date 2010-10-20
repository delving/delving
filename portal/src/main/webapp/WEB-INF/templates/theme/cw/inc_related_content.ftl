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
                    <a href="${doc.fullDocUrl()}?${queryStringForPaging?html}&amp;start=${doc.index()?c}&amp;uri=${doc.id}&amp;view=${view}&amp;startPage=1&amp;pageId=brd&amp;tab=">
                        <#else>
                        <a href="${doc.fullDocUrl()}">
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
                    <a href="${doc.fullDocUrl()}?${queryStringForPaging?html}&amp;start=${doc.index()?c}&amp;uri=${doc.id}&amp;startPage=1&amp;pageId=brd"><@stringLimiter "${doc.title}" "50"/></a>
                    <#else>
                        <a href="${doc.fullDocUrl()}?"><@stringLimiter "${doc.title}" "50"/></a>
                </#if>
            </td>
        </tr>
    </#list>
</table>

<#if result.relatedItems?size &gt; max>
    <p>
        <a href='/${portalName}/brief-doc.html?query=europeana_uri:"${uri}"&amp;view=${view}'>
        <@spring.message 'SeeAllRelatedItems_t' /></a>
    </p>
</#if>

<div class="clear"></div>

<h3><@spring.message 'Actions_t'/>:</h3>

    <p>
        <!-- AddThis Button BEGIN -->

        <a
                href="http://www.addthis.com/bookmark.php?v=250&username=collectiewijzer"
                class="addthis_button"
                style="text-decoration:none;"
                addthis:title="${result.fullDoc.dcTitle[0]}"
         >
            <img src="http://s7.addthis.com/static/btn/sm-plus.gif"  width="16" height="16" border="0" alt="Share" />
            Deel deze pagina
        </a>
        <script type="text/javascript" src="http://s7.addthis.com/js/250/addthis_widget.js#username=collectiewijzer"></script>
        <script type="text/javascript">
            var addthis_config = {
                ui_language: "nl",
                ui_click: true,
                ui_cobrand: "Collectiewijzer",
                ui_header_color: "#ffffff",
                ui_header_background: "#046f96"


            }
        </script>
        <!-- AddThis Button END -->
    </p>

<#if user??>
    <p>
        <a href="#" onclick="saveItem('SavedItem','${postTitle?js_string}','${postAuthor?js_string}','${result.fullDoc.id?js_string}','${result.fullDoc.thumbnails[0]?js_string}','${result.fullDoc.europeanaType}');">
            Bewaar dit object
        </a>
    </p>    
    <span id="msg-save-item" class="hide"></span>

    <div class="clear"></div>

    <h6><@spring.message 'AddATag_t' /></h6>

    <form action="#" method="post" onsubmit="addTag('SocialTag', document.getElementById('tag').value,'${result.fullDoc.id}','${result.fullDoc.thumbnails[0]?js_string}','${postTitle}','${result.fullDoc.europeanaType}'); return false;" id="form-addtag" name="form-addtag" accept-charset="UTF-8">
        <input type="text" name="tag" id="tag" maxlength="50" class="text"/>
        <input type="submit" class="button tag" value="<@spring.message 'Add_t' />"/>
    </form>

    <span id="msg-save-tag" class="hide"></span>

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
            <a href="login.html" class="disabled" onclick="highLight('mustlogin'); showMessage('#mustLoginMsg', 'U moet inloggen of registreren'); return false;">Bewaar</a>
        </p>

        <p>
            <a href="login.html" class="disabled" onclick="highLight('mustlogin'); showMessage('#mustLoginMsg', 'U moet inloggen of registreren'); return false;"><@spring.message 'AddATag_t' /></a>
        </p>
        <div id="mustLoginMsg" class="hide"></div>

    </div>
</#if>