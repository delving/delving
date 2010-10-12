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
            <input type="hidden" name="formType" id="formType" value="Login"/>
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
                    <td>
                        <a id="forgotPassword" href="#" class="frm_head"
                        onclick="showForgotPassword();"><@spring.message 'ForgotPassword_t' /></a>

                    </td>
                    <td align="right"><input name="submit_login" type="submit" value="<@spring.message 'LogIn_t' />" class="button"/></td>

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

    <#if failureForgotFormat || failureForgotDoesntExist || forgotSuccess>
        <div id="request-password" class="grid_4 login-register">
    <#else >
        <div style="display:none;" id="request-password" class="grid_4 login-register">
    </#if>
    <h2>Wachtwoord aanvragen</h2>

    <form id="forgotemailForm" name='f2' action='' method='POST' accept-charset="UTF-8">
        <input type="hidden" name="formType" id="formType" value="Request"/>
    <table>
        <tr>
            <td><label for="email"><@spring.message 'EmailAddress_t' /></label></td>
            <td><input id="email-forgot" type="text" name="email" value="" maxlength="50"></td>
        </tr>
        <tr>
            <td></td>
            <td><input id="submit_forgot" name="submit_login" type="submit" value="Aanvragen" class="button"/></td>
        </tr>

    </table>

    <#if forgotSuccess>
        <p id="forgotSuccess" class="success">
        <@spring.message 'AnEmailHasBeenSentTo_t' />: <span class="fg-gold">${email}</span>.
        <@spring.message 'PleaseFollowTheLinkProvided_t' />.  <!-- TODO change message -->
        </p>
    </#if>
    <#if failureForgotFormat>
                    <div class="ui-widget">
                        <div class="ui-state-error ui-corner-all" style="padding: 0pt 0.7em;">
                            <span class="ui-icon ui-icon-alert" style="float: left; margin-right: 0.3em;"></span>
        <@spring.message 'Error_t' />!<br/><@spring.message 'EmailFormatError_t' />.
        </div>
    </#if>
    <#if failureForgotDoesntExist>
                    <div class="ui-widget">
                        <div class="ui-state-error ui-corner-all" style="padding: 0pt 0.7em;">
                            <span class="ui-icon ui-icon-alert" style="float: left; margin-right: 0.3em;"></span>
        <@spring.message 'Error_t' />!<br/>EmailDoesntExist_t <!-- TODO add message -->
        </div>
    </#if>
    </form>
</div>

    <div id="register-div" class="grid_4 login-register">

        <h2><@spring.message 'Register_t' /></h2>
        <#if register == true>
            <form id="registrationForm" name='f3' action='' method='POST' accept-charset="UTF-8">
                <input type="hidden" name="formType" id="formType" value="Register"/>
                <table>
                    <tr>
                        <td width="100"><label for="email"><@spring.message 'EmailAddress_t' /></label></td>
                        <td><input id="register_email" type='text' name='email' id="email" value='' accept-charset="UTF-8"></td>
                    </tr>
                    <tr>
                        <td></td>
                        <td><input id="register" name="submit_button" type="submit" value="<@spring.message 'Register_t' />"
                           class="button"/></td>
                    </tr>

                </table>

                <#if success>
                    <p id="success" class="success">
                    <@spring.message 'AnEmailHasBeenSentTo_t' />: <span class="fg-gold">${email}</span>.
                    <@spring.message 'PleaseFollowTheLinkProvided_t' />.
                    </p>
                </#if>

                <#if failureFormat>
                    <div class="ui-widget">
                        <div class="ui-state-error ui-corner-all" style="padding: 0pt 0.7em;">
                            <span class="ui-icon ui-icon-alert" style="float: left; margin-right: 0.3em;"></span>
                            <strong><@spring.message 'Error_t' />: </strong><@spring.message 'EmailFormatError_t' />.
                        </div>
                    </div>
                </#if>

                <#if failureExists>
                    <div class="ui-widget">
                        <div class="ui-state-error ui-corner-all" style="padding: 0pt 0.7em;">
                            <span class="ui-icon ui-icon-alert" style="float: left; margin-right: 0.3em;"></span>
                            <strong><@spring.message 'Error_t' />: </strong><@spring.message 'EmailAlreadyRegistered_t' />.
                        </div>
                    </div>
                </#if>
            </form>
        <#else>
            Registration is not enabled yet.
        </#if>
    </div>

    <div class="clear"></div>


</div>


<#include "inc_footer.ftl"/>

