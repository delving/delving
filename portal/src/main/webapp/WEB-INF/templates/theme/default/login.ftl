<#import "spring_form_macros.ftl" as spring />
<#include "spring_form_macros.ftl"/>
<#assign thisPage = "login.html"/>
<#--<#assign register = register>-->

<#include "inc_header.ftl">

<div id="header">

    <div id="identity" class="grid_3">
        <h1>Delving</h1>
        <a href="/${portalName}/index.html" title="ABM"><img src="/${portalName}/${portalTheme}/images/abm-logo.jpg" alt="ABM"/></a>
    </div>

    <div class="grid_9">

        <div id="top-bar">
            <div class="inner">
                <@userbar/>
            </div>
        </div>

    </div>

</div>

<div id="main" class="grid_10 prefix_4">


    <div id="login-div" class="grid_5 alpha login-register">


        <h2><@spring.message '_mine.login' /></h2>

        <form id="loginForm" name='f1' action='j_spring_security_check' method='POST' accept-charset="UTF-8">
        <table>
            <tr>
                <td><label for="j_username"><@spring.message '_mine.email.address' /></label></td>
                <td><input type='text' id="j_username" name="j_username" value="" maxlength="50"></td>
            </tr>
            <tr>
                <td><label for="j_password"><@spring.message "_register.password" /></label></td>
                <td><input type='password' id="j_password" name='j_password' maxlength="50"/></td>
            </tr>
            <tr>
                <td>
                    <a id="forgotPassword" href="/${portalName}/forgot-password.html" class="frm_head""><@spring.message '_mine.forgotpassword' /></a>
                </td>
                <td align="right"><input name="submit_login" type="submit" value="<@spring.message '_mine.login' />" class="button"/></td>
            </tr>
        </table>
        <#if errorMessage>
            <div class="ui-widget">
                <div class="ui-state-error ui-corner-all" style="padding: 0pt 0.7em;">
                    <span class="ui-icon ui-icon-alert" style="float: left; margin-right: 0.3em;"></span>
                    <strong><@spring.message '_portal.ui.notification.error' />: </strong> Inlog gegevens zijn niet juist
                </div>
            </div>
        </#if>


</div>



</div>

<#include "inc_footer.ftl"/>

