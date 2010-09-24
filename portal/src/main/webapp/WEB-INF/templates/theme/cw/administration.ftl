<#import "spring.ftl" as spring />
<#assign thisPage = "administration.html"/>
<#assign pageId = "ad"/>
<#include "inc_header.ftl"/>

<div id="main">

    <div id="administration-page" class="grid_16">

        <form method="post" action="administration.html" id="administration-form">
            <table>
            <#if !userList??>
                <tr>
                    <td>Vind een gebruiker (email)</td>
                    <td><input type="text" name="searchPattern"/></td>
                    <td><input type="submit" value="vind"/> </td>
                </tr>
            </#if>
            <#if userList??>
                <tr>
                    <th>Email</th>
                    <th>Huidige Rol</th>
                    <th>Nieuwe Rol</th>
                </tr>
                <#list userList as user>
                    <tr>
                        <td><input type="text" name="" value="${user.email}"/></td>
                        <td>${user.role}</td>
                        <td>
                            <select name="newRole">
                                <option>Kies een rol</option>
                                <option value="ROLE_RESEARCH_USER">Researcherr</option>
                                <option value="ROLE_ADMINISTRATOR">Administrator</option>
                                <option value="ROLE_USER">User</option>
                            </select>
                        </td>
                    </tr>
                </#list>
            </#if>
            </table>

        </form>

    </div>

</div>

<#include "inc_footer.ftl"/>