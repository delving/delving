<#compress>
<div id="main" class="static-page">

    <#if pagePathList??>
        <#assign thisPage = "static-page.dml"/>
        <#assign pageId = "static"/>
        <#include "inc_header.ftl"/>
        <div class="grid_16">
            <h2>Bestaande paginas</h2>

            <table summary="List of existing pages">
                <#assign pages = pagePathList?sort />
                <#list pages as pagePath>
                    <tr>
                        <td><a href="${pagePath}?edit=true"><span class="ui-icon ui-icon-document"></span></a></td>
                        <td width="300"><a href="${pagePath}?edit=true">${pagePath}</a></td>
                    </tr>
                </#list>
            </table>
        </div>
        <div class="clear"></div>
        <#include "inc_footer.ftl"/>

    <#elseif embedded>
        ${content}
    <#else>

        <#assign thisPage = "static-page.dml"/>
        <#assign pageId = "static"/>
        <#include "inc_header.ftl"/>

        <div class="grid_12">
            <div id="content" class="content-preview">
            ${content}
            </div>
            <#if edit??>
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
                            </table>
                        </form>
                    </div>
                <#else>
                    <p><a href="${pagePath}?edit=true" class="button">Edit this page.</a></p>
                </#if>
            </#if>
        </div>
        <script type="text/javascript" src="/${portalName}/${portalTheme}/js/tiny_mce/tiny_mce.js"></script>
        <script type="text/javascript" src="/${portalName}/${portalTheme}/js/static-page.js"></script>

        <#include "inc_footer.ftl"/>
    </#if>
</div>
</#compress>