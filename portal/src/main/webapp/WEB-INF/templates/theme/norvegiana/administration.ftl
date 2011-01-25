<#import "spring.ftl" as spring />
<#assign thisPage = "administration.html"/>
<#assign pageId = "ad"/>
<#include "includeMarcos.ftl">

<@addHeader "${portalDisplayName}", "",["jquery.tablesorter.min.js","administration.js"],[]/>

<section role="main" class="main grid_12" id="administration-page">


        <h1><@spring.message '_cms.administration.users' /></h1>

        <div class="grid_4 alpha">



            <form method="post" action="administration.html" id="search-form" name="search-form">
                <fieldset>
                    <legend><@spring.message '_cms.user.find' /></legend>
                <table>
                    <tr>
                        <td width="150"><input type="text" id="searchPattern" name="searchPattern"/></td>
                        <td><input type="submit" class="btn-strong" value="<@spring.message '_cms.find' />"/> </td>
                    </tr>
                </table>
                </fieldset>
            </form>
           <#if userList?? && userList?size=0>
               <h6><@spring.message '_cms.user.not.found'/></h6>
           </#if>
        </div>

        <#if userList?? && (userList?size &gt; 0)>
        <div class="grid_12 alpha">
            <table  class="tablesorter zebra" width="100%" id="tbl-users-found">
                <thead>
                <tr>
                    <th><@spring.message '_mine.email.address' /></th>
                    <th><@spring.message '_mine.username' /></th>
                    <th><@spring.message '_cms.user.role.current' /></th>
                    <th><@spring.message '_cms.user.role.new' /></th>
                    <th></th>
                    <th></th>
                </tr>
                </thead>
                <tbody>

                <#list userList as userEdit>
                  <form method="post" action="administration.html" class="set-form" name="set-form_${userEdit_index}"
                        <input type="hidden" class="formIndex" value="${userEdit_index}"/>
                        <tr id="row_${userEdit_index}">
                            <td width="150">${userEdit.email}<input type="hidden" name="userEmail" value="${userEdit.email}"/></td>
                            <td width="150">${userEdit.userName}</td>
                            <td width="150">
                                <#switch userEdit.role>
                                    <#case "ROLE_GOD">
                                        <@spring.message '_cms.user.role.super' />
                                    <#break>
                                    <#case "ROLE_ADMINISTRATOR">
                                          <@spring.message '_cms.user.role.administrator' />
                                    <#break>
                                    <#case "ROLE_USER">
                                          <@spring.message '_cms.user.role.public' />
                                    <#break>
                                </#switch>
                            </td>
                            <td width="200" id="uRoleList">
                                <select name="newRole" class="newRole">
                                    <option value="NONE"><@spring.message '_cms.user.role.choose' /></option>
                                    <#if user.role=="ROLE_GOD"><option value="ROLE_ADMINISTRATOR"><@spring.message '_cms.user.role.administrator' /></option></#if>
                                    <option value="ROLE_USER"><@spring.message '_cms.user.role.public' /></option>
                                </select>
                            </td>
                            <td><input type="submit" class="button btn-strong" value="<@spring.message '_cms.change' />" class="btn-strong"/> </td>
                            <td><button class="btn-strong delete" type="button" value="${userEdit_index}"><@spring.message '_cms.delete' /></button> </td>
                        </tr>
                   </form>
                </#list>

                </tbody>
            </table>

          </div>
        </#if>

        <div id="all-users-list" class="grid_12 alpha"></div>

</section>

<script type="text/javascript">
    <#if targetUser??>

        var msgString = "${targetUser.email} now has the role of: ";
            <#if targetUser.role = 'ROLE_ADMINISTRATOR'>
                msgString += "<@spring.message '_cms.user.role.administrator' />";
            </#if>
            <#if targetUser.role = 'ROLE_USER'>
                msgString += "<@spring.message '_cms.user.role.public' />";
            </#if>
        showMessage("success",msgString);

    </#if>
    <#if RequestParameters.userRemoved??>
        <#if RequestParameters.userRemoved = "true">
            showMessage("success","User has been successfully removed");
        </#if>
    </#if>

    $('button.delete').click(function() {
        var targetRow = "#row_"+$(this).val();
        var targetMail = $(targetRow+" input[name=userEmail]").val();
        showConfirm("notification", "<strong>"+targetMail+"</strong><br/><@spring.message '_cms.user.remove.confirm'/>", "removeUser('" + targetMail + "')")
    });
</script>
<@addFooter/>
