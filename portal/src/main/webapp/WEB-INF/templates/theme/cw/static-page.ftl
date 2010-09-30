<#compress>
    <#if pageNameList??>
        <#assign thisPage = "static-page.html"/>
        <#assign pageId = "static"/>
        <#include "inc_header.ftl"/>
        <div class="main">
        <ul>
            <#list pageNameList as pageName>
                <li><a href="_${pageName}.html">_${pageName}.html</a></li>
            </#list>
        </ul>
        </div>
        <#include "inc_footer.ftl"/>
    <#elseif onlyContent??>
        ${content}
    <#else>
        <#assign thisPage = "static-page.html"/>
        <#assign pageId = "static"/>
        <#include "inc_header.ftl"/>
        <div class="main">

            <div id="search" class="grid_12">

                <div class="static_page">

                    <#if content??>
                        <div id="content">
                        ${content}
                        </div>
                    </#if>
                    <#if edit??>
                        <#if edit>
                            <div id="pageForm">
                                <form action="_${pageName}.html" method="POST">
                                    <table>
                                        <tr>
                                            <td>
                                                <textarea name="content" cols="130" rows="30">${content}</textarea>
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
                                <p><a href="_${pageName}.html?edit=true">Edit this page.</a></p>
                        </#if>
                    </#if>

                </div>

            </div>
        </div>
        <#include "inc_footer.ftl"/>
    </#if>
</#compress>