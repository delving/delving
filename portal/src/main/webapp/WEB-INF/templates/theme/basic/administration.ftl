<#import "spring.ftl" as spring />
<#assign thisPage = "administration.html"/>
<#assign pageId = "ad"/>



            <#include "includeMarcos.ftl">

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
                <h1><@spring.message '_cms.administration.users' /></h1>
            </header>

            <div id="administration-page" class="grid_9">

            <ol>
                <li><@spring.message '_cms.user.change.role.step.1' /></li>
                <li><@spring.message '_cms.user.change.role.step.2' /></li>
                <li><@spring.message '_cms.user.change.role.step.3' /></li>
                <li><@spring.message '_cms.user.change.role.step.4' /></li>
            </ol>






        <#if !userList??>

            <form method="post" action="administration.html" id="search-form">
                <table width="400">
                    <tr>
                        <th colspan="2"><@spring.message '_cms.user.find' /></th>
                     </tr>
                    <tr></tr>
                        <td><input type="search" name="searchPattern"/></td>
                        <td><input type="submit" value="<@spring.message '_cms.find' />"/> </td>
                    </tr>
                </table>
                <#if targetUser??>
                    <p>
                        ${targetUser.email} <@spring.message '_cms.user.role.set.to' />: 
                        <#if targetUser.role = 'ROLE_ADMINISTRATOR'>
                            <@spring.message '_cms.user.role.administrator' />
                        </#if>
                        <#if targetUser.role = 'ROLE_USER'>
                            <@spring.message '_cms.user.role.public' />
                        </#if>
                    </p>
                </#if>
            </form>
        </#if>

        <#if userList??>
            <#if userList?size &gt; 0>
                <table>
                    <tr>
                        <th><@spring.message '_mine.email.address' /></th>
                        <th><@spring.message '_cms.user.role.current' /></th>
                        <th><@spring.message '_cms.user.role.new' /></th>
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
                                <td width="200">
                                    <select name="newRole" id="newRole">
                                        <option value="NONE"><@spring.message '_cms.user.role.choose' /></option>
                                        <#if user.role=="ROLE_GOD"><option value="ROLE_ADMINISTRATOR"><@spring.message '_cms.user.role.administrator' /></option></#if>
                                        <option value="ROLE_USER"><@spring.message '_cms.user.role.public' /></option>
                                    </select>
                                </td>
                                <td><input type="submit" value="<@spring.message '_cms.change' />"/> </td>
                            </tr>
                        </form>
                    </#list>
                </table>
            <#else>
                <h4><@spring.message '_cms.user.not.found' /></h4>
                <p><a href="/${portalName}/administration.html"><@spring.message '_cms.user.find' /></a></p>
            </#if>
        </#if>


    </div>

</section>

<script type="text/javascript">
    $("form#set-form").submit(function(){
        if($("select#newRole").val()=="NONE"){
            return false;
        }

    })
</script>

<@addFooter/>
