<#compress>
    <#if imagePathList??>
        <#if javascript>
            var tinyMCEImageList = new Array(
            <#list imagePathList as imagePath>
                 ["${imagePath}","${imagePath}"]<#if imagePath_has_next>,</#if>
            </#list>
            );
        <#else>
            <#assign thisPage = "static-image.dml"/>
            <#assign pageId = "static"/>
            <#include "includeMarcos.ftl">

            <@addHeader "${portalDisplayName}", "",["static-image.js"],[]/>
            <script type="text/javascript">
                var deleteConfirm = "<@spring.message '_cms.image.delete.question' />";
                var deleteFail = "<@spring.message '_cms.image.delete.fail' />";
            </script>

            <section role="main" class="grid_12" >

                <h1><@spring.message '_cms.administration.images' /></h1>

                <div class="grid_8 alpha">
                     <table summary="List of existing images" class="user-images zebra">
                        <thead>
                            <tr>
                                <th colspan="4"><@spring.message '_cms.existing.images' /></th>
                            </tr>
                        </thead>
                        <#list imagePathList as imagePath>
                            <tr id="${imagePath_index}">
                                <td width="50"><img src="${imagePath}" alt="thumbnail" height="20"/></td>
                                <td width="300"><a href="${imagePath}?edit=true"><span class="ui-icon ui-icon-image"></span>${imagePath}</a></td>
                                <td width="85"><a href="${imagePath}?edit=true"><span class="ui-icon ui-icon-pencil"></span><@spring.message '_cms.edit' /></a></td>
                                <td width="85">
                                     <a class="delete" name="${imagePath_index}" href="${imagePath}"><span class="ui-icon ui-icon-trash"></span><@spring.message '_cms.delete' /></a>
                                </td>
                            </tr>
                        </#list>
                    </table>
                </div>


                <div class="grid_4 omega">

                    <fieldset>
                        <h2><@spring.message '_cms.image.create' /></h2>
                        <#--<legend><@spring.message '_cms.image.create' /></legend>-->

                    <form method="POST" action="/${portalName}/images/_.img" enctype="multipart/form-data">
                        <table>
                            <tr>
                                <td><input type="file" name="file" size="30"/></td>
                            </tr>
                            <tr>
                                <td><input type="submit" name="submit" class="button" value="<@spring.message '_cms.upload' />"></td>
                            </tr>
                        </table>
                    </form>
                      </fieldset>

                </div>
            </section>

            <div class="clear"></div>
            <@addFooter/>
        </#if>
    <#else>
        <#assign thisPage = "static-image.img"/>
        <#assign pageId = "static"/>

        <#include "includeMarcos.ftl">

        <@addHeader "${portalDisplayName}", "",[],[]/>


            <section role="main" class="grid_12">


                <h1><@spring.message '_cms.administration.images' /></h1>


                <div class="grid_5 alpha">
                    <#if imageExists>
                        <img src="/${portalName}/${imagePath}" alt="${imagePath}" style="max-width:100%"/><br/>

                    <#else>
                        <p><@spring.message '_cms.image.not.exist' /></p>
                    </#if>
                </div>

                <#--<div class="clear"></div>-->

                <div class="grid_7 omega">
                    <#if edit??>
                        <#if edit>
                            <div id="pageForm">
                                <h6 class="ui-widget ui-state-highlight">${imagePath}</h6>
                                <form method="POST" enctype="multipart/form-data">
                                    <table>
                                        <tr>
                                            <td width="200" align="right"><@spring.message '_cms.image.choose' /></td>
                                            <td><input type="file" name="file" size="30"/></td>
                                        </tr>
                                        <tr>
                                            <td></td>
                                            <td><input type="submit" name="submit" class="button" value="<@spring.message '_cms.upload' />" class="btn-strong"></td>
                                        </tr>
                                    </table>
                                </form>
                            </div>
                            <div id="pageForm2">
                                <form method="POST">
                                    <table>
                                        <tr>
                                            <td width="200" align="right"><@spring.message '_cms.image.new.path'/></td>
                                            <td><input type="newPath" name="newPath" value="${imagePath}" size="30"/></td>
                                        </tr>
                                        <tr>
                                            <td></td>
                                            <td><input type="submit" name="submit" class="button" value="<@spring.message '_cms.image.rename'/>"  class="btn-strong"></td>
                                        </tr>
                                    </table>
                                </form>
                            </div>
                        <#else>
                            <a href="/${portalName}/${imagePath}?edit=true" class="button"><@spring.message '_cms.image.change' /></a>
                        </#if>
                            <a href="/${portalName}/_.img" class="button"><@spring.message '_cms.image.list' /></a>
                    </#if>

                </div>

            </section>

        <@addFooter/>
    </#if>
</#compress>
