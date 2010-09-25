<#import "spring.ftl" as spring />
<#assign thisPage = "administration.html"/>
<#assign pageId = "ad"/>
<#include "inc_header.ftl"/>

<div id="main">

    <div id="administration-page" class="grid_16">

        <#if !userList??>
            <form method="post" action="administration.html" id="search-form">
                <table>
                    <tr>
                        <td>Vind een gebruiker</td>
                        <td><input type="text" name="searchPattern"/></td>
                        <td><input type="submit" value="vind"/> </td>
                    </tr>
                </table>
                <#if targetUser??>
                    <p>
                        ${targetUser.email} heeft nu de rol van
                        <#if targetUser.role = 'ROLE_RESEARCH_USER'>
                            Museometrie Gebruiker
                        </#if>
                        <#if targetUser.role = 'ROLE_ADMINISTRATOR'>
                            Administrator
                        </#if>
                        <#if targetUser.role = 'ROLE_USER'>
                            Gewoon Gebruiker
                        </#if>
                    </p>
                </#if>
            </form>
        </#if>

        <#if userList??>
            <table>
                <tr>
                    <th>Email</th>
                    <th>Huidige Rol</th>
                    <th>Nieuwe Rol</th>
                    <th>Zetten</th>
                </tr>
                <#list userList as user>
                    <form method="post" action="administration.html" id="set-form">
                        <input type="hidden" name="userEmail" value="${user.email}"/>
                        <tr>
                            <td>${user.email}</td>
                            <td>${user.role}</td>
                            <td>
                                <select name="newRole">
                                    <option>Kies een rol</option>
                                    <option value="ROLE_RESEARCH_USER">Museometrie Gebruiker</option>
                                    <option value="ROLE_ADMINISTRATOR">Administrator</option>
                                    <option value="ROLE_USER">Gewoon Gebruiker</option>
                                </select>
                            </td>
                            <td><input type="submit" value="zet nu"/> </td>
                        </tr>
                    </form>
                </#list>
            </table>
        </#if>
    </div>

</div>

<#include "inc_footer.ftl"/>