<#compress>
    <#if pagePathList??>
        <#assign thisPage = "static-page.dml"/>
        <#assign pageId = "static"/>
        <#include "inc_header.ftl"/>
        <div class="main">
        <ul>
            <#list pagePathList as pagePath>
                <li><a href="${pagePath}">${pagePath}</a></li>
            </#list>
        </ul>
        </div>
        <#include "inc_footer.ftl"/>
    <#elseif edit??>
        <#assign thisPage = "static-page.dml"/>
        <#assign pageId = "static"/>
        <#include "inc_header.ftl"/>
        <div class="main">
            <div id="search" class="grid_12">
                <div class="static_page">
                    <#if content??>
                        <div id="content" class="content-preview">
                        ${content}
                        </div>
                    </#if>
                    <#if edit>
                        <div id="pageForm">
                            <form action="${pagePath}" method="POST">
                                <table>
                                    <tr>
                                        <td>
                                            <textarea name="content" id="editor" style="width:100%;height:350px;"${content}</textarea>
                                            <input type="submit" name="submit">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <a href="javascript:toggleEditor('editor');" class="button">Show/Hide HTML editor</a>
                                            <a href="${pagePath}?edit=false" class="button">Abort edit.</a>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="background-color:pink;">
                                            <p>An image list that WS Melvin could put in the editor?:</p>
                                            <ul>
                                                <#list imagePathList as imagePath>
                                                    <li>${imagePath}</li>
                                                </#list>
                                            </ul>
                                        </td>
                                    </tr>
                                </table>
                            </form>
                        </div>
                    <#else>
                        <p><a href="${pagePath}?edit=true" class="button">Edit this page.</a></p>
                    </#if>
                </div>
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
                theme_advanced_buttons1 : "bold,italic,underline,strikethrough,|,justifyleft,justifycenter,justifyright,justifyfull,|,formatselect|,bullist,numlist,|,undo,redo,|,link,unlink,anchor,|,image,|,forecolor,backcolor",
                theme_advanced_buttons2 : "",
                theme_advanced_statusbar_location : "bottom",
                content_css : "/${portalName}/${portalTheme}/css/reset-text-grid.css,/${portalName}/${portalTheme}/css/type.css,/${portalName}/${portalTheme}/css/color.css,/${portalName}/${portalTheme}/css/screen.css"
            });

            function toggleEditor(id) {
                if (!tinyMCE.get(id)){
                    tinyMCE.execCommand('mceAddControl', false, id);
                }else{
                    tinyMCE.execCommand('mceRemoveControl', false, id);
                }
            }
        </script>
    <#else>
    ${content}
    </#if>
</#compress>