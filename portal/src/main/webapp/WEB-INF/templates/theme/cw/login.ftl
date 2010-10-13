<#import "spring.ftl" as spring />
<#include "spring_form_macros.ftl"/>
<#assign thisPage = "login.html"/>
<#assign register = register>

<#include "inc_header.ftl">

<div id="main">


    <div id="login-div" class="grid_4 login-register">

        <h2><@spring.message 'LogIn_t' /></h2>
        <#if register == true>
            <form id="loginForm" name='f1' action='j_spring_security_check' method='POST' accept-charset="UTF-8">
            <table>
                <tr>
                    <td><label for="j_username"><@spring.message 'EmailAddress_t' /></label></td>
                    <td><input type='text' id="j_username" name="j_username" value="" maxlength="50"></td>
                </tr>
                <tr>
                    <td><label for="j_password"><@spring.message "Password_t" /></label></td>
                    <td><input type='password' id="j_password" name='j_password' maxlength="50"/></td>
                </tr>
                <tr>

                    <td align="right" colspan="2">
                        <input name="submit_login" type="submit" value="<@spring.message 'LogIn_t' />" class="button"/>
                    </td>

                </tr>
                <tr>
                    <td colspan="2"><a id="forgotPassword" href="/${portalName}/forgot-password.html"><@spring.message 'ForgotPassword_t' /></a></td>
                </tr>
            </table>




                    <#if errorMessage??>
                        <div class="ui-widget">
                            <div class="ui-state-error ui-corner-all" style="padding: 0pt 0.7em;">
                                <span class="ui-icon ui-icon-alert" style="float: left; margin-right: 0.3em;"></span>
                                <strong><@spring.message 'Error_t' />: </strong> Inlog gegevens zijn niet juist
                            </div>
                        </div>
                    </#if>
        <#else>
            Login is not enabled yet.
        </#if>
    </div>

    <div class="clear"></div>


</div>


<#include "inc_footer.ftl"/>

