<#import "spring.ftl" as spring />
<#include "spring_form_macros.ftl"/>
<#assign thisPage = "login.html"/>
<#include "inc_header.ftl">

<div id="doc4" class="yui-t2">
    <div id="hd">
        <#include "inc_top_nav.ftl"/>
    </div>
    <div id="bd">
        <div id="yui-main">

            <div class="yui-b">
                <div class="yui-g" id="mainContent">
                    <div class="yui-u first">

                        <#assign register = register>

                        <h1><@spring.message 'LogIn_t' /></h1>
                        <#if register == true>
                        <form id="loginForm" name='f1' action='j_spring_security_check' method='POST' accept-charset="UTF-8">
                            <fieldset>
                                <legend></legend>
                                <label for="j_username"><@spring.message 'EmailAddress_t' /></label>
                                <input type='text' id="j_username" name="j_username" value="" maxlength="50">
                                <label for="j_password"><@spring.message "Password_t" /></label>
                                <input type='password' id="j_password" name='j_password' maxlength="50"/>
                                <#-- _spring_security_remember_me should always be true.
                                    The user shouldn't have an option to uncheck it.
                                    That's why this is a hidden field and not a checkbox -->
                                <input class="inline" type='hidden' value="true" id='_spring_security_remember_me'
                                       name='_spring_security_remember_me'/>
                                <input name="submit_login" type="submit" value="<@spring.message 'LogIn_t' />" class="button"/>
                                <#if errorMessage??><div class="ui-widget ui-error"><strong>${errorMessage}</strong></div> </#if>
                                <a id="forgotPassword" href="#" class="frm_head"
                                   onclick="toggleObject('fp')"><@spring.message 'ForgotPassword_t' /></a>
                            </fieldset>
                        </form>
                          <#else>
                        Login is not enabled yet.
                    </#if>

                        <#if failureForgotFormat || failureForgotDoesntExist || forgotSuccess>
                        <div id="fp">
                        <#else >
                        <div style="display:none;" id="fp">
                        </#if>
                            <h1>Request password</h1>
                            <form id="forgotemailForm" name='f2' action='' method='POST' accept-charset="UTF-8">
                                <fieldset>
                                    <legend></legend>
                                    <label for="forgot_email"><@spring.message 'EmailAddress_t' /></label>
                                    <input id="forgot_email" type="text" name="email" value="" maxlength="50">
                                    <input id="submit_forgot" name="submit_login" type="submit" value="Request" class="button"/>
                                </fieldset>
                                <#if forgotSuccess>
                                <div id="forgotSuccess" class="ui-widget ui-info">
                                    <@spring.message 'AnEmailHasBeenSentTo_t' />: <span class="fg-gold">${email}</span>.
                                    <@spring.message 'PleaseFollowTheLinkProvided_t' />.  <!-- TODO change message -->
                                </div>
                                </#if>
                                <#if failureForgotFormat>
                                <div class="ui-widget ui-error">
                                    <strong><@spring.message 'Error_t' />!</strong>  <@spring.message 'EmailFormatError_t' />.
                                </div>
                                </#if>
                                <#if failureForgotDoesntExist>
                                <div class="ui-widget ui-error">
                                    <@spring.message 'Error_t' />!<br/> EmailDoesntExist_t <!-- TODO add message -->
                                </div>
                                </#if>
                            </form>
                        </div>
                    </div>

                    <div class="yui-u">

                        <h1><@spring.message 'Register_t' /></h1>
                        <#if register == true>
                        <form id="registrationForm" name='f3' action='' method='POST' accept-charset="UTF-8">
                            <input type="hidden" name="submit_login" value="Register"/>
                            <fieldset>
                                <legend></legend>
                                <label for="email"><@spring.message 'EmailAddress_t' /></label>
                                <input id="register_email" type='text' name='email' value='' accept-charset="UTF-8">
                                <input id="register" name="submit_button" type="submit" value="<@spring.message 'Register_t' />" class="button"/>

                            </fieldset>
                            <#if success>
                            <div id="success" class="ui-widget ui-info">
                                <@spring.message 'AnEmailHasBeenSentTo_t' />: <span class="fg-gold">${email}</span>.
                                <@spring.message 'PleaseFollowTheLinkProvided_t' />.
                            </div>
                            </#if>
                            <#if failureFormat>
                            <div class="ui-widget ui-error">
                                <strong><@spring.message 'Error_t' />!</strong> <@spring.message 'EmailFormatError_t' />.
                            </div>
                            </#if>
                            <#if failureExists>
                            <div class="ui-widget ui-error">
                                <strong><@spring.message 'Error_t' />!</strong> <@spring.message 'EmailAlreadyRegistered_t' />.
                            </div>
                            </#if>
                        </form>
                        <#else>
                        Registration is not enabled yet.
                        </#if>

                    </div>
                </div>


            </div>
        </div>
        <div class="yui-b">
            <#include "inc_logo_sidebar.ftl"/>

        </div>
    </div>
    <div id="ft">
        <#include "inc_footer.ftl"/>
    </div>
</div>
</body>
</html>
