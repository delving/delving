<#import "spring.ftl" as spring />
<#include "spring_form_macros.ftl"/>
<#assign thisPage = "forgot-password.html"/>

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

<div id="main">

    <div id="request-password" class="grid_4 login-register">

        <h2>Wachtwoord aanvragen</h2>

        <form id="forgotPasswordForm" name="forgotPasswordForm" action="forgot-password.html" method="POST" accept-charset="UTF-8">
            <table>
                <tr>
                    <td><label for="email"><@spring.message '_prompt.email.address' /></label></td>
                    <td><input id="email" type="text" name="email" value="" maxlength="50"></td>
                </tr>
                <tr>
                    <td></td>
                    <td><input id="submit_forgot" name="submit_login" type="submit" value="Aanvragen" class="button"/></td>
                </tr>
            </table>
        </form>

        <#if state == "success">
            <p id="forgotSuccess" class="success">
                <@spring.message '_register.email.has.been.sent' />: <span class="fg-gold">${email}</span>.
                <@spring.message 'PleaseFollowTheLinkProvided_t' />.  <!-- TODO change message -->
            </p>
        </#if>
        <#if state == "formatFailure">
            <div class="ui-widget">
                <div class="ui-state-error ui-corner-all" style="padding: 0pt 0.7em;">
                    <span class="ui-icon ui-icon-alert" style="float: left; margin-right: 0.3em;"></span>
                    <@spring.message 'Error_t' />!<br/><@spring.message 'EmailFormatError_t' />.
                </div>
            </div>
        </#if>
        <#if state == "nonexistentFailure">
            <div class="ui-widget">
                <div class="ui-state-error ui-corner-all" style="padding: 0pt 0.7em;">
                    <span class="ui-icon ui-icon-alert" style="float: left; margin-right: 0.3em;"></span>
                    <@spring.message 'Error_t' />!<br/>EmailDoesntExist_t <!-- TODO add message -->
                </div>
            </div>
        </#if>

    </div>

</div>

<div class="clear"></div>

<#include "inc_footer.ftl"/>

