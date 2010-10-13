<#import "spring.ftl" as spring />
<#include "spring_form_macros.ftl"/>
<#assign thisPage = "forgot-password.html"/>
<#assign register = register>

<#include "inc_header.ftl">

<div id="main">



    <div id="request-password" class="grid_4 login-register">

    <h2>Wachtwoord aanvragen</h2>

    <form id="forgotemailForm" name="forgotemailForm" action="login.html" method="POST" accept-charset="UTF-8">
        <input type="hidden" name="formType" id="formType" value="Request"/>

        <table>
            <tr>
                <td><label for="email"><@spring.message 'EmailAddress_t' /></label></td>
                <td><input id="email" type="text" name="email" value="" maxlength="50"></td>
            </tr>
            <tr>
                <td></td>
                <td><input id="submit_forgot" name="submit_login" type="submit" value="Aanvragen" class="button"/></td>
            </tr>

        </table>
    </form>

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

</div>

<div class="clear"></div>

<#include "inc_footer.ftl"/>

