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
            <#include "delving-macros.ftl">

            <@addHeader "Norvegiana", "",[],[]/>

            <section id="sidebar" class="grid_3" role="complementary">
                <header id="branding" role="banner">
                    <h1 class="large">${portalDisplayName}</h1>
                </header>

                <div id="search" role="search">
                    <@simpleSearch/>
                </div>


            </section>

            <section id="main" class="grid_9" role="main">

                <div class="grid_6">
                    <h2><@spring.message 'dms.existing.images' /></h2>
                     <table summary="List of existing images" class="user-options">
                        <#list imagePathList as imagePath>
                            <tr>
                                <td width="50"><img src="${imagePath}" alt="thumbnail" height="20"/></td>
                                <td width="300"><a href="${imagePath}?edit=true"><span class="ui-icon ui-icon-image"></span>${imagePath}</a></td>
                                <td width="85"><a href="${imagePath}?edit=true"><span class="ui-icon ui-icon-pencil"></span><@spring.message 'dms.edit' /></a></td>
                                <td width="85">
                                     <a class="delete" id="delete_${imagePath_index}" href="${imagePath}"><span class="ui-icon ui-icon-trash"></span><@spring.message 'dms.delete' /></a>
                                </td>
                            </tr>
                        </#list>
                    </table>
                </div>


                <div class="grid_6">

                    <h2><@spring.message 'dms.image.create' /></h2>
                    <form method="POST" action="/${portalName}/images/_.img" enctype="multipart/form-data">
                        <table>
                            <tr>
                                <td><input type="file" name="file" size="30"/></td>
                            </tr>
                            <tr>
                                <td><input type="submit" name="submit" value="<@spring.message 'dms.upload' />"></td>
                            </tr>
                        </table>
                    </form>
                    
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
                            var confirmation = confirm("<@spring.message 'dms.image.delete.question' />")
                            if(confirmation){
                                $.ajax({
                                    url: targetURL+"?edit=false&delete=true",
                                    type: "GET",
                                    success: function(data) {
                                        window.location.reload();
                                    },
                                    error: function(data) {
                                        alert("<@spring.message 'dms.image.delete.fail' />");
                                    }
                                });
                            }
                            return false;
                        });
                    </script>
                </div>
            </section>

            <div class="clear"></div>
            <#include "inc_footer.ftl"/>
        </#if>
    <#else>
        <#assign thisPage = "static-image.img"/>
        <#assign pageId = "static"/>

        <#include "delving-macros.ftl">

        <@addHeader "Norvegiana", "",[],[]/>

        <section id="sidebar" class="grid_3" role="complementary">
            <header id="branding" role="banner">
                <h1 class="large">${portalDisplayName}</h1>
            </header>

            <div id="search" role="search">
                <@simpleSearch/>
            </div>


        </section>


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
                            <p><a href="/${portalName}/${imagePath}?edit=true"><@spring.message 'dms.image.change' /></a></p>
                        </#if>
                        <p><a href="_.img"><@spring.message 'dms.image.list' /></a></p>
                    </#if>

                </div>

            </div>

        <#include "inc_footer.ftl"/>
    </#if>
</#compress>