<#compress>
    <#if imagePathList??>
        <#if javascript>
            var tinyMCEImageList = new Array(
            <#list imagePathList as imagePath>
                 ["${imagePath}","${imagePath}"]<#if imagePath_has_next>,</#if>
            </#list>
            );

        <#else>
            <#assign thisPage = "static-page.dml"/>
            <#assign pageId = "static"/>
            <#include "inc_header.ftl"/>
            <div class="main">
            <ul>
                <#list imagePathList as imagePath>
                    <li><a href="${imagePath}?edit=false">${imagePath}</a></li>
                </#list>
            </ul>
            </div>
            <#include "inc_footer.ftl"/>
        </#if>
    <#else>
        <#assign thisPage = "static-image.img"/>
        <#assign pageId = "static"/>
        <#include "inc_header.ftl"/>
        <div class="main">

            <div id="image" class="grid_12">

                <div class="static_image">
                    <#if imageExists>
                        <img src="${imagePath}" alt="${imagePath}"/>
                    <#else>
                        <p>This image does not exist</p>
                    </#if>
                    <#if edit??>
                        <#if edit>
                            <div id="pageForm">
                                <form method="POST" enctype="multipart/form-data">
                                    <table>
                                        <tr>
                                            <td>Upload a new image for this URL</td>
                                            <td><input type="file" name="file" size="80"/></td>
                                        </tr>
                                        <tr>
                                            <td></td>
                                            <td><input type="submit" name="submit"></td>
                                        </tr>
                                    </table>
                                </form>
                            </div>
                        <#else>
                            <p><a href="${imagePath}?edit=true">Change this image</a></p>
                        </#if>
                        <p><a href="_.img">List images</a></p>
                    </#if>

                </div>

            </div>
        </div>
        <#include "inc_footer.ftl"/>
    </#if>
</#compress>