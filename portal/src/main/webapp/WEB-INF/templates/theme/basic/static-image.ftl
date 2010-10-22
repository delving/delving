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

            <div id="header">

                <div id="identity" class="grid_3">
                    <h1>Delving</h1>
                    <a href="/${portalName}/index.html" title="Delving"><img src="/${portalName}/${portalTheme}/images/logo-small.png" alt="Delving Home"/></a>
                </div>

                <div class="grid_9">

                    <div id="top-bar">
                        <div class="inner">
                            <@userbar/>
                        </div>
                    </div>

                </div>

            </div>

            <div class="main">

                <div class="grid_6">
                    <h2><@spring.message 'dms.existing.images' /></h2>
                     <table summary="List of existing images" class="user-options">
                        <#list imagePathList as imagePath>
                            <tr>
                                <td width="50"><img src="/${portalName}/${imagePath}" alt="thumbnail" height="20"/></td>
                                <td width="300"><a href="/${portalName}/${imagePath}?edit=true"><span class="ui-icon ui-icon-image"></span>${imagePath}</a></td>
                                <td width="85"><a href="/${portalName}/${imagePath}?edit=true"><span class="ui-icon ui-icon-pencil"></span><@spring.message 'dms.edit' /><</a></td>
                                <td width="85">
                                     <a class="delete" id="delete_${imagePath_index}" href="/${portalName}/${imagePath}?edit=false&delete=true"><span class="ui-icon ui-icon-trash"></span><@spring.message 'dms.delete' /></a>
                                </td>
                            </tr>
                        </#list>
                    </table>
                </div>
            </div>

            <div class="grid_6">
                <h2><@spring.message 'dms.image.create' /></h2>
                <ol>
                    <li><@spring.message 'dms.image.create.step.1' /></li>
                    <li>
                        <@spring.message 'dms.image.create.step.2' />
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
                <input type="submit" value="<@spring.message 'dms.goto.upload' />"/>
                </form>
                <#--<h2>Nieuwe afbeelding aanmaken</h2>-->
                <#--<div id="uploadForm">-->
                    <#--<form method="POST" action="/${portalName}/images/_.img" enctype="multipart/form-data">-->
                        <#--<table>-->
                            <#--<tr>-->
                                <#--<td width="200">Nieuwe afbeelding kiezen</td>-->
                                <#--<td><input type="file" name="file" size="30"/></td>-->
                            <#--</tr>-->
                            <#--<tr>-->
                                <#--<td></td>-->
                                <#--<td><input type="submit" name="submit" value="Afbeelding uploaden"></td>-->
                            <#--</tr>-->
                        <#--</table>-->
                    <#--</form>-->
                <#--</div>-->
                <script type="text/javascript">
                    function createImage(){
                        var name = $("#imgName").attr("value");
                        var ext = $("#imgExt :selected").text();
                        var pName = $("#pName").attr("value");
                        var makeURL = pName+name+ext+".img";
                        window.location.href=makeURL+"?edit=true";
                    }
                    
                    $("a.delete").click(function(){
                        var target = $(this).attr("id");
                        var targetURL = $(this).attr("href");
                        var confirmation = confirm("Afbeelding: "+targetURL +" verwijderen ?")
                        if(confirmation){
                            $.ajax({
                                url: targetURL,
                                type: "GET",
                                data: "content=",
                                success: function(data) {
                                    window.location.reload();
                                },
                                error: function(data) {
                                    alert("Image could not be deleted");
                                }
                            });
                            return false;
                        } else {
                            return false;
                        }
                    });
                </script>
            </div>

            <div class="clear"></div>
            <#include "inc_footer.ftl"/>
        </#if>
    <#else>
        <#assign thisPage = "static-image.img"/>
        <#assign pageId = "static"/>
        <#include "inc_header.ftl"/>
             <div id="header">

                <div id="identity" class="grid_3">
                    <h1>Delving</h1>
                    <a href="/${portalName}/index.html" title="Delving"><img src="/${portalName}/${portalTheme}/images/logo-small.png" alt="Delving Home"/></a>
                </div>

                <div class="grid_9">

                    <div id="top-bar">
                        <div class="inner">
                            <@userbar/>
                        </div>
                    </div>

                </div>

            </div>
        <div id="main" style="padding-top: 2em">



                <div class="grid_12">
                    <#if imageExists>
                        <img src="/${portalName}/${imagePath}" alt="${imagePath}" width="100%"/>
                    <#else>
                        <p><@spring.message 'dms.image.not.exist' /></p>
                    </#if>
                </div>

                <div class="clear"></div>

                <div class="grid_12">
                    <#if edit??>
                        <#if edit>
                            <div id="pageForm">
                                <form method="POST" enctype="multipart/form-data">
                                    <table>
                                        <tr>
                                            <td width="200"><@spring.message 'dms.image.choose' /></td>
                                            <td><input type="file" name="file" size="30"/></td>
                                        </tr>
                                        <tr>
                                            <td></td>
                                            <td><input type="submit" name="submit" value="<@spring.message 'dms.upload' />"></td>
                                        </tr>
                                    </table>
                                </form>
                            </div>
                            <div id="pageForm2">
                                <form method="POST">
                                    <table>
                                        <tr>
                                            <td width="200">Nieuwe afbeelding URL</td>
                                            <td><input type="newPath" name="newPath" value="${imagePath}" size="30"/></td>
                                        </tr>
                                        <tr>
                                            <td></td>
                                            <td><input type="submit" name="submit" value="Afbeelding hernoemen"></td>
                                        </tr>
                                    </table>
                                </form>
                            </div>
                        <#else>
<<<<<<< HEAD:portal/src/main/webapp/WEB-INF/templates/theme/basic/static-image.ftl
                            <p><a href="${imagePath}?edit=true"><@spring.message 'dms.image.change' /></a></p>
=======
                            <p><a href="/${portalName}/${imagePath}?edit=true">Verander deze afbeelding</a></p>
>>>>>>> 5208bea... Image upload workflow changed: default location is /images/<fileName>, and it can be changed anytime:portal/src/main/webapp/WEB-INF/templates/theme/cw/static-image.ftl
                        </#if>
                        <p><a href="_.img"><@spring.message 'dms.image.list' /></a></p>
                    </#if>

                </div>

            </div>

        <#include "inc_footer.ftl"/>
    </#if>
</#compress>