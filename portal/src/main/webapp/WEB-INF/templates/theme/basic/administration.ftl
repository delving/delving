<#import "spring.ftl" as spring />
<#assign thisPage = "administration.html"/>
<#assign pageId = "ad"/>
<#include "includeMarcos.ftl">

<@addHeader "Delving", "",["jquery.tablesorter.min.js","administration.js"],[]/>

<section role="main" class="main grid_12" id="administration-page">

    <header>
        <h1><@spring.message 'dms.administration.users' /></h1>
    </header>

    <#--

    <div class="grid_8">
            <ol>
                <li><@spring.message 'dms.user.change.role.step.1' /></li>
                <li><@spring.message 'dms.user.change.role.step.2' /></li>
                <li><@spring.message 'dms.user.change.role.step.3' /></li>
                <li><@spring.message 'dms.user.change.role.step.4' /></li>
            </ol>
    </div>

    <div class="grid_4">
            <form method="post" action="administration.html" id="search-form">
                <table width="400">
                    <tr>
                        <th colspan="2"><@spring.message 'dms.user.find' /></th>
                     </tr>
                    <tr></tr>
                        <td><input type="search" name="searchPattern"/></td>
                        <td><input type="submit" value="<@spring.message 'dms.find' />"/> </td>
                    </tr>
                </table>
                <#if targetUser??>
                    <p>
                        ${targetUser.email} <@spring.message 'dms.user.role.set.to' />:
                        <#if targetUser.role = 'ROLE_ADMINISTRATOR'>
                            <@spring.message 'dms.user.role.administrator' />
                        </#if>
                        <#if targetUser.role = 'ROLE_USER'>
                            <@spring.message 'dms.user.role.public' />
                        </#if>
                    </p>
                </#if>
            </form>
    </div>

    

        <#if userList??>
            <#if userList?size &gt; 0>
                <table>
                    <tr>
                        <th><@spring.message 'EmailAddress_t' /></th>
                        <th><@spring.message 'dms.user.role.current' /></th>
                        <th><@spring.message 'dms.user.role.new' /></th>
                        <th>&#160;</th>
                    </tr>

                    <#list userList as userEdit>
                        <form method="post" action="administration.html" id="set-form">
                            <input type="hidden" name="userEmail" value="${userEdit.email}"/>
                            <tr>
                                <td width="150">${userEdit.email}</td>
                                <td width="150">
                                    <#switch userEdit.role>
                                        <#case "ROLE_GOD">
                                            <@spring.message 'dms.user.role.super' />
                                        <#break>
                                        <#case "ROLE_ADMINISTRATOR">
                                              <@spring.message 'dms.user.role.administrator' />
                                        <#break>
                                        <#case "ROLE_USER">
                                              <@spring.message 'dms.user.role.public' />
                                        <#break>
                                    </#switch>
                                </td>
                                <td width="200">
                                    <select name="newRole" id="newRole">
                                        <option value="NONE"><@spring.message 'dms.user.role.choose' /></option>
                                        <#if user.role=="ROLE_GOD"><option value="ROLE_ADMINISTRATOR"><@spring.message 'dms.user.role.administrator' /></option></#if>
                                        <option value="ROLE_USER"><@spring.message 'dms.user.role.public' /></option>
                                    </select>
                                </td>
                                <td><input type="submit" value="<@spring.message 'dms.change' />"/> </td>
                            </tr>
                        </form>
                    </#list>
                </table>
            <#else>
                <h4><@spring.message 'dms.user.not.found' /></h4>
                <p><a href="/${portalName}/administration.html"><@spring.message 'dms.user.find' /></a></p>
            </#if>
        </#if>


    </div>

    -->
         <div class="grid_6 alpha">
            <ol>
                <li><@spring.message 'dms.user.change.role.step.1' /></li>
                <li><@spring.message 'dms.user.change.role.step.2' /></li>
                <li><@spring.message 'dms.user.change.role.step.3' /></li>
                <li><@spring.message 'dms.user.change.role.step.4' /></li>
            </ol>

                <#if targetUser??>
                    <script type="text/javascript">
                     var message = "${targetUser.email} heeft nu de rol van ";
                        <#if targetUser.role = 'ROLE_RESEARCH_USER'>
                            message += "Museometrie Gebruiker";
                        </#if>
                        <#if targetUser.role = 'ROLE_ADMINISTRATOR'>
                            message += "Administrator";
                        </#if>
                        <#if targetUser.role = 'ROLE_USER'>
                            message += "Gewone Gebruiker";
                        </#if>
                        showMessage("success",message);
                    </script>
                    <h4>



                    </h4>
                </#if>

        </div>

        <div class="grid_6 omega">


         <h2>Vind een gebruiker</h2>
            <form method="post" action="administration.html" id="search-form">
                <table>
                    <tr>
                        <td width="150"><input type="text" id="searchPattern" name="searchPattern"/></td>
                        <td><input type="submit" class="btn-strong" value="<@spring.message 'dms.find' />"/> </td>
                    </tr>
                </table>
            </form>
           <#if userList?? && userList?size=0>
               <h4>Geen gebruiker(s) gevonden</h4>
           </#if>
        </div>

        <#if userList?? && (userList?size &gt; 0)>
        <div class="grid_12 alpha">
            <table class='tablesorter'>
                <tr>
                    <th>Email</th>
                    <th>Huidige Rol</th>
                    <th>Nieuwe Rol</th>
                    <th>Rol aanpassen</th>
                    <th>Gebruiker verwijderen</th>
                </tr>
                <#list userList as userEdit>
                    <form method="post" action="administration.html" id="set-form">
                        <input type="hidden" id="userEmail" name="userEmail" value="${userEdit.email}"/>
                        <tr>
                            <td width="150">${userEdit.email}</td>
                            <td width="150">
                                <#switch userEdit.role>
                                    <#case "ROLE_GOD">
                                        Super User
                                    <#break>
                                    <#case "ROLE_RESEARCH_USER">
                                         Museometrie Gebruiker
                                    <#break>
                                    <#case "ROLE_ADMINISTRATOR">
                                          Administrator
                                    <#break>
                                    <#case "ROLE_USER">
                                          Gewone Gebruiker
                                    <#break>
                                </#switch>
                            </td>
                            <td width="200" id="uRoleList">
                                <select name="newRole">
                                    <option>Kies een rol</option>
                                    <#if user.role=="ROLE_GOD"><option value="ROLE_ADMINISTRATOR">Administrator</option></#if>
                                    <option value="ROLE_RESEARCH_USER">Museometrie Gebruiker</option>
                                    <option value="ROLE_USER">Gewone Gebruiker</option>
                                </select>
                            </td>
                            <td><input type="submit" value="Rol aanpassen" class="btn-strong"/> </td>
                            <td><button id="rem-user" class="btn-strong delete">Gebruiker verwijderen</button> </td>
                        </tr>
                    </form>
                </#list>
            </table>

          </div>
        </#if>

        <div id="all-users-list" class="grid_12 alpha"></div>

</section>

<script type="text/javascript">
    $("form#set-form").submit(function(){
        if($("select#newRole").val()=="NONE"){
            return false;
        }

    })
</script>

<@addFooter/>