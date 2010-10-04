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

                <div class="grid_6">
                    <h2>Bestaande Afbeelingen</h2>
                     <table summary="List of existing images">
                        <#list imagePathList as imagePath>
                            <tr>
                                <td width="5"><a href="${imagePath}?edit=true"><img src="${imagePath}" height="35" alt="Thumbnail"/></a></td>
                                <td width="300"><a href="${imagePath}?edit=true">${imagePath}</a></td>
                                <td width="65"><a href="${imagePath}?edit=true">Bewerken</a></td>
                                <#--<td width="65">-->
                                     <#--<a class="delete" id="delete_${imagePath_index}" href="${imagePath}">Verwijder</a>-->

                                <#--</td>-->
                            </tr>
                        </#list>
                    </table>
                </div>
            </div>

            <div class="grid_6">
                <h2>Nieuwe afbeelding aanmaken</h2>
                <ol>
                    <li>Kies eerst een naam en extensie voor de nieuwe afbeelding</li>
                    <li>
                        Druk op "Ga naar upload" om dan een afbeelding van uw filesysteem te uploaden.
                    </li>
                </ol>
                <form action="" method="get" onsubmit="createImage();return false;">
                    <input type="hidden" value="/${portalName}/" id="pName"/>
                    <label>Naam:</label><input type="text" name="imgName" id="imgName" value=""/>
                    <select id="imgExt" name="imgExt">
                        <option>.jpg</option>
                        <option>.png</option>
                        <option>.gif</option>
                    </select>
                <input type="submit" value="Ga naar upload"/>
                </form>
                <script type="text/javascript">
                    function createImage(){
                        var name = $("#imgName").attr("value");
                        var ext = $("#imgExt :selected").text();
                        var pName = $("#pName").attr("value");
                        var makeURL = pName+name+ext+".img";
                        window.location.href=makeURL+"?edit=true";
                    }
                </script>
            </div>

            <div class="clear"></div>
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