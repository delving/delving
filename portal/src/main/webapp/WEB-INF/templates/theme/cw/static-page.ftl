<#compress>
<div id="main" class="static-page">

    <#if pagePathList??>

        <#assign thisPage = "static-page.dml"/>
        <#assign pageId = "static"/>
        <#include "inc_header.ftl"/>
        <div class="grid_6">
            <h2>Bestaande paginas</h2>

            <table summary="List of existing pages">
                <#list pagePathList as pagePath>
                    <tr>
                        <td><a href="${pagePath}?edit=true"><span class="ui-icon ui-icon-document"></span></a></td>
                        <td width="300"><a href="${pagePath}?edit=true">${pagePath}</a></td>
                    </tr>
                </#list>
            </table>
        </div>

        <div class="grid_6">

            <h2>Maak een nieuwe pagina</h2>
            <label>Pagina pad & naam: </label>${portalName}/&#160;<input type="text" value="" name="pagePath" id="pagePath"/>.dml<br />

            <a href="" class="button" id="makePage">Aanmaken</a>

        </div>

        <div class="clear"></div>

        <script type="text/javascript">
              $("#makePage").click(function(){
                 pageToMake = $("#pagePath").val()+".dml?edit=true";
                    window.location.href = pageToMake;
                  return false;
              })
        </script>

        <#include "inc_footer.ftl"/>

    <#elseif embedded>

        ${page.content}

    <#else>

        <#assign thisPage = "static-page.dml"/>
        <#assign pageId = "static"/>
        <#include "inc_header.ftl"/>
        <div class="grid_12">
            <div id="content" class="content-preview">
            ${page.content}
            </div>
            <#if edit??>
                <#if edit>
                    <div id="pageForm">
                        <form action="${page.path}" method="POST">
                            <table>
                                <tr>
                                    <td>
                                        <textarea name="content" id="editor" style="width:100%;height:350px;"${page.content}</textarea>
                                        <input type="submit" name="submit">
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <a href="javascript:toggleEditor('editor');" class="button">Show/Hide HTML editor</a>
                                        <a href="${page.path}?edit=false" class="button">Abort edit.</a>
                                    </td>
                                </tr>
                            </table>
                        </form>
                    </div>
                <#else>
                    <#if page.id??>
                        <p><a href="${page.path}?edit=true&version=${page.id}" class="button">Edit this page.</a></p>
                    <#else>
                        <p><a href="${page.path}?edit=true" class="button">Edit this page.</a></p>
                    </#if>
                    <br/><br/>
                    <#if versionList?? && page.id??>
                        <h3>Versions</h3>
                        <ul>
                            <#list versionList as version>
                                <#if version.id == page.id>
                                    <li><strong>${version.date?string("yyyy-MM-dd HH:mm:ss")}</strong> - <a href="${version.path}?version=${version.id}&edit=false&approve=true">Approve this version</li>
                                <#else>
                                    <li><a href="${version.path}?version=${version.id}&edit=false">${version.date?string("yyyy-MM-dd HH:mm:ss")}</a></li>
                                </#if>
                            </#list>
                        </ul>
                    </#if>
                </#if>
            </#if>
        </div>
        <script type="text/javascript" src="/${portalName}/${portalTheme}/js/tiny_mce/tiny_mce.js"></script>
        <script type="text/javascript" src="/${portalName}/${portalTheme}/js/static-page.js"></script>
        <#include "inc_footer.ftl"/>
        
    </#if>
</div>
</#compress>