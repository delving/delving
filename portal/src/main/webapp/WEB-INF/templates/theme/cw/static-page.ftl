<#compress>

    <#if pagePathList??>

        <#assign thisPage = "static-page.dml"/>
        <#assign pageId = "static"/>
        <#include "inc_header.ftl"/>

        <div id="main" class="static-page">

        <div class="grid_6">
            <h2>Bestaande paginas</h2>

            <table summary="List of existing pages" class="user-options">
                <#list pagePathList as pagePath>
                    <tr>
                        <td width="300">
                            <a href="${pagePath}?edit=true">
                            <span class="ui-icon ui-icon-document"></span>
                            ${pagePath}</a></td>
                        <td width="85"><a href="${pagePath}?edit=true">
                            <span class="ui-icon ui-icon-pencil"></span>
                            Bewerken</a>
                        </td>
                        <td width="100">
                             <a class="delete" id="delete_${pagePath_index}" href="${pagePath}">
                                <span class="ui-icon ui-icon-trash"></span>
                                 Verwijder
                             </a>

                        </td>
                    </tr>
                </#list>
            </table>
        </div>
        
        <div class="grid_6">

            <h2>Maak een nieuwe pagina</h2>
            <p>
                Voor het aanmaken van een pagina vul in het pad (als gewenst) en de naam van de nieuwe pagina.
                De naam van de pagina <strong>moet</strong> eindigen met <strong>.dml</strong>
            </p>
            <p>
                Het basis pad <strong>/${portalName}</strong> wordt automatisch aangemaakt. Daarop volgende paden zijn
                niet verplicht maar kunnen wel helpen met het ordennen en overzicht van de paginas.
            </p>
            <form method="get" action="" id="form-makePage" onsubmit="createPage(this.pagePath.value);return false;">
                /${portalName}/&#160;<input type="text" value="" name="pagePath" id="pagePath"/>
                <input type="submit" value="Aanmaken" id="makePage"/>
            </form>

       </div>


        <div class="clear"></div>

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
                        <form action="${page.path}" method="POST" id="form-edit">
                            <table>
                                <tr>
                                    <td>
                                      <a href="javascript:toggleEditor('editor');" class="">Show/Hide HTML editor</a>  
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <textarea name="content" id="editor" style="width:100%;height:350px;"${page.content}</textarea>
                                        <input type="submit" name="submit" value="Bewaar"/> <a href="${page.path}?edit=false" class="button">Cancel</a>
                                    </td>
                                </tr>
                            </table>
                        </form>
                    </div>
                <#else>
                    <#if page.id??>
                        <p><a href="${page.path}?edit=true&version=${page.id}" class="button">Pagina bewerken</a></p>
                    <#else>
                        <p><a href="${page.path}?edit=true" class="button">Pagina bewerken</a></p>
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