

            <h5><@spring.message '_header.actions' />:</h5>


        <#if addThisTrackingCode??>
        <!-- AddThis Button BEGIN -->
        <div class="addthis_toolbox addthis_default_style">
            <a href="http://www.addthis.com/bookmark.php?v=250&amp;username=${addThisTrackingCode}" class="addthis_button_compact">Share</a>
        </div>
        <script type="text/javascript" src="http://s7.addthis.com/js/250/addthis_widget.js#username=${addThisTrackingCode}"></script>
        <!-- AddThis Button END -->
        <script type="text/javascript">
        var addthis_config = {
             ui_language: "no",
            ui_click: true,
            ui_cobrand: "Norvegiana",
            ui_header_color: "#ffffff",
            ui_header_background:"#0071BC"
        }
        </script>
        <br/>
        </#if>


            <#if user??>


                    <a href="inc_related_content.ftl#" onclick="saveItem('SavedItem','${postTitle?js_string}','${postAuthor?js_string}','${result.fullDoc.id?js_string}','${result.fullDoc.thumbnails[0]?js_string}','${result.fullDoc.europeanaType}');return false;"><@spring.message 'SaveToPersonalPage' /></a>
                    <div id="msg-save-item" class="msg-hide"></div>


                <#if result.fullDoc.europeanaType == "IMAGE">
                	<#if result.fullDoc.europeanaIsShownBy[0]?? && imageAnnotationToolBaseUrl?? && imageAnnotationToolBaseUrl!="">

		                    <a href="${imageAnnotationToolBaseUrl}?user=${user.userName}&objectURL=${result.fullDoc.europeanaIsShownBy[0]}&id=${result.fullDoc.id}" target="_blank"><@spring.message '_action.add.annotation' /></a>

	                </#if>
                </#if>

            <h6><@spring.message '_action.add.tag' /></h6>

                <#--<div id="ysearchautocomplete">-->
                      <form action="inc_related_content.ftl#" method="post" onsubmit="addTag('SocialTag', document.getElementById('tag').value,'${result.fullDoc.id}','${result.fullDoc.thumbnails[0]?js_string}','${postTitle}','${result.fullDoc.europeanaType}'); return false;"  id="form-addtag" name="form-addtag" accept-charset="UTF-8">
                        <input type="text" name="tag" id="tag" maxlength="50" class="text"/>
                        <input type="submit" class="button" value="Add"/>
                    </form>
                    <div id="msg-save-tag" class="hide"></div>
                <#--</div>-->


        <#else>
            <div class="related-links">
                <p>
                    <a  href="/${portalName}/login.html" class="disabled" onclick="highLight('#mustlogin'); return false;"><@spring.message '_action.add.tag' /></a>
                </p>

                <p>
                    <a  href="/${portalName}/login.html" class="disabled" onclick="highLight('#mustlogin'); return false;"><@spring.message 'SaveToPersonalPage' /></a>
                </p>
            </div>

        </#if>

