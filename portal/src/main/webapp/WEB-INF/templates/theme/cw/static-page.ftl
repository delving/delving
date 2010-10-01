<#compress>
    <#if pagePathList??>
        <#assign thisPage = "static-page.dml"/>
        <#assign pageId = "static"/>
        <#include "inc_header.ftl"/>
        <div class="main grid_16">
            <h2>Existing pages</h2>
            <ul>
                <#list pagePathList as pagePath>
                    <li><a href="${pagePath}">${pagePath}</a></li>
                </#list>
            </ul>
        </div>
        <#include "inc_footer.ftl"/>
    <#elseif onlyContent??>
        ${content}
    <#else>
        <#assign thisPage = "static-page.dml"/>
        <#assign pageId = "static"/>
        <#include "inc_header.ftl"/>
        <div class="main">

            <div class="static_page">

                <#if content??>
                    <div id="content" class="content-preview">
                    ${content}
                    </div>
                    <div class="clear"></div>
                </#if>
                <#if edit??>
                    <div id="pageForm" class="grid_16">

                            <form action="${pagePath}" method="POST">

                                <textarea name="content" id="editor" style="width:940px;height:350px;"${content}</textarea>

                                <input type="submit" name="submit" value="Bewaar">
                                <a href="javascript:toggleEditor('editor');">Show/Hide HTML editor</a>
                            </form>
                        </div>
                        <div class="clear"></div>
                        <#else>
                        <p><a href="${pagePath}?edit=true" class="button">Edit this page.</a></p>
                    </div>
                 </#if>

            </div>
        </div>

        <#include "inc_footer.ftl"/>

        <script type="text/javascript" src="/${portalName}/${portalTheme}/js/tiny_mce/tiny_mce.js"></script>
        <script type="text/javascript">

            tinyMCE.init({
                mode : "textareas",
                theme : "advanced",
                theme_advanced_toolbar_location : "top",
                theme_advanced_toolbar_align : "left",
                theme_advanced_buttons1 : "bold,italic,underline,strikethrough,|,justifyleft,justifycenter,justifyright,justifyfull,|,formatselect,|,bullist,numlist,|,undo,redo,|,link,unlink,anchor,|,image,|,forecolor,backcolor,|,removeformat,code",
                theme_advanced_buttons2 : "",
                theme_advanced_statusbar_location : "bottom",
                content_css : "/${portalName}/${portalTheme}/css/reset-text-grid.css,/${portalName}/${portalTheme}/css/type.css,/${portalName}/${portalTheme}/css/screen.css"
            });

            function toggleEditor(id) {
                if (!tinyMCE.get(id)) {
                    tinyMCE.execCommand('mceAddControl', false, id);
                } else {
                    tinyMCE.execCommand('mceRemoveControl', false, id);
                }
            }
        </script>
    </#if>
</#compress>