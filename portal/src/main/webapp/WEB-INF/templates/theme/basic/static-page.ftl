<#compress>

    <#if pagePathList??>

        <#if RequestParameters.javascript??>

            var tinyMCELinkList = new Array(
            <#list pagePathList as pagePath>
                 ["${pagePath}","${pagePath}"]<#if pagePath_has_next>,</#if>
            </#list>
            );

        <#else>

            <#assign thisPage = "static-page.dml"/>
            <#assign pageId = "static"/>
            <#include "delving-macros.ftl">

            <@addHeader "Norvegiana", "",[],[]/>

            <section id="sidebar" class="grid_3" role="complementary">
                <header id="branding" role="banner">
                    <a href="/${portalName}/" title=""/>
                        <img src="/${portalName}/${portalTheme}/images/norvegiana.jpg" alt="Norvegiana"/>
                    </a>
                    <h1 class="large">${portalDisplayName}</h1>
                </header>
            </section>


            <section role="main">

            <header>
                <h1><@spring.message 'dms.administration.pages' /></h1>
            </header>

                <div class="grid_5 alpha">
                    <h2><@spring.message 'dms.existing.pages' /></h2>
                    <table summary="List of existing pages" class="user-options" width="100%">
                        <#list pagePathList as pagePath>
                            <tr>
                                <td width="300">
                                    <a href="${pagePath}?edit=true">
                                    <span class="ui-icon ui-icon-document"></span>
                                    ${pagePath}</a></td>
                                <td width="85"><a href="${pagePath}?edit=true">
                                    <span class="ui-icon ui-icon-pencil"></span>
                                    <@spring.message 'dms.edit' /></a>
                                </td>
                                <td width="100">
                                     <a class="delete" id="delete_${pagePath_index}" href="${pagePath}?delete=true">
                                        <span class="ui-icon ui-icon-trash"></span>
                                          <@spring.message 'dms.delete' />
                                     </a>

                                </td>
                            </tr>
                        </#list>
                    </table>
                </div>

                <div class="grid_4 omega">

                    <h2><@spring.message 'dms.page.create' /></h2>
                    <ol>
                        <li><@spring.message 'dms.page.create.step.1' /></li>
                        <li><@spring.message 'dms.page.create.step.2' /></li>
                    </ol>
                    <form method="get" action="" id="form-makePage" onsubmit="createPage(this.pagePath.value);return false;">
                        /${portalName}/&#160;<input type="text" value="" name="pagePath" id="pagePath"/>
                        <input type="submit" value="<@spring.message 'dms.create' />" id="makePage"/>
                    </form>

               </div>

            </section>


            <script type="text/javascript">

                $("a.delete").click(function(){
                    var target = $(this).attr("id");
                    var targetURL = $(this).attr("href");
                    var confirmation = confirm("Pagina: "+targetURL +" verwijderen ?")
                    if(confirmation){
                        $.ajax({
                            url: targetURL,
                            type: "POST",
                            data: "content=",
                            success: function(data) {
                                window.location.reload();
                            },
                            error: function(data) {
                                alert("page could not be deleted");
                            }
                        });
                        return false;
                    } else {
                        return false;
                    }
                });

                function createPage(page){
                    var targetURL = $("#pagePath").attr("value");
                    window.location.href=targetURL+"?edit=true";
                }

            </script>

            <@addFooter/>

        </#if>

    <#elseif embedded>

        ${page.content}

    <#else>

        <#assign thisPage = "static-page.dml"/>
        <#assign pageId = page.path/>
            <#include "delving-macros.ftl">

            <@addHeader "Norvegiana", "",[],[]/>


            <section id="sidebar" class="grid_2" role="complementary">
                <header id="branding" role="banner">
                    <a href="/${portalName}/" title=""/>
                    <img src="/${portalName}/${portalTheme}/images/norvegiana.jpg" alt="Norvegiana"/>
                    </a>
                    <h1 class="large">${portalDisplayName}</h1>
                </header>
            </section>


            <section role="main" class="grid_10">

                <#if edit??>
                    <#if edit>
                        <div id="pageForm">
                            <form action="${page.path}" method="POST" id="form-edit">
                                <a href="javascript:toggleEditor('editor');" class=""><@spring.message 'dms.html.editor.show.hide' /></a>
                                <textarea name="content" id="editor" style="width: 100%;height:550px;">${page.content}</textarea>
                                <input type="submit" name="submit" value="<@spring.message 'dms.save' />"/>
                                <a href="${page.path}" class="button"><@spring.message 'dms.cancel' /></a>
                            </form>
                        </div>
                    <#else>
                        <#if page.id??>
                            <p><a href="${page.path}?edit=true&version=${page.id}" class="button"><@spring.message 'dms.page.edit' /></a></p>
                        <#else>
                            <p><a href="${page.path}?edit=true" class="button"><@spring.message 'dms.page.edit' /></a></p>
                        </#if>

                        <p><a href="/${portalName}/_.dml" class="button"><@spring.message 'dms.page.list' /></a></p>

                        <#if versionList?? && page.id??>
                            <h3><@spring.message 'dms.version.management' /></h3>
                            <p>
                                <@spring.message 'dms.version.approve.explain' />
                            </p>
                            <ul>
                                <#list versionList as version>
                                    <#if version.id == page.id>
                                        <li><strong>${version.date?string("yyyy-MM-dd HH:mm:ss")}</strong> - <a href="${version.path}?version=${version.id}&edit=false&approve=true"><@spring.message 'dms.version.approve' /></li>
                                    <#else>
                                        <li><a href="${version.path}?version=${version.id}&edit=false">${version.date?string("yyyy-MM-dd HH:mm:ss")}</a></li>
                                    </#if>
                                </#list>
                            </ul>
                        </#if>
                    </#if>
                </#if>
            </section>
    
        <script type="text/javascript" src="/${portalName}/${portalTheme}/js/tiny_mce/tiny_mce.js"></script>
        <script type="text/javascript" src="/${portalName}/${portalTheme}/js/static-page.js"></script>
        <@addFooter/>
     </div>   
    </#if>



</#compress>