<#compress>
    <#assign thisPage = "static-image.img"/>
    <#assign pageId = "static"/>
    <#include "inc_header.ftl"/>
    <div class="main">

        <div id="image" class="grid_12">

            <div class="static_image">
                <#if imageExists>
                    <img src="${imagePath}?onlyContent=true" alt="${imagePath}?onlyContent=true"/>
                <#else>
                    <p>This image does not exist</p>
                </#if>
                <#if edit??>
                    <#if edit>
                        <div id="pageForm">
                            <form method="POST" enctype="multipart/form-data">
                                <table>
                                    <tr>
                                        <td>
                                            <input type="file" name="file"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <input type="submit" name="submit">
                                        </td>
                                    </tr>
                                </table>
                            </form>
                        </div>
                    <#else>
                        <p><a href="${imagePath}?edit=true">Upload this image</a></p>
                    </#if>
                </#if>

            </div>

        </div>
    </div>
    <#include "inc_footer.ftl"/>
</#compress>