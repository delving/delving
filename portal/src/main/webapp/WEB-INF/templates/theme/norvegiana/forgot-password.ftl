<#import "spring.ftl" as spring />
<#assign thisPage = "forgot-password.html"/>

<#include "includeMarcos.ftl">

<@addHeader "${portalDisplayName}", "",[],[]/>

<section role="main" class="grid_4 prefix_4">

<form action="forgot-password.html" method="POST" accept-charset="UTF-8">
    <fieldset>
        <legend> <@spring.message '_mine.forgotpassword' /></legend>
    <table>
        <tr>
            <td><label for="email"><@spring.message '_mine.email.address' /></label></td>
            <td><input id="email" type="text" name="email" value="" maxlength="50"></td>
        </tr>
        <tr>
            <td></td>
            <td><input id="submit_forgot" name="submit_login" type="submit" value="<@spring.message '_action.form.submit' />"/></td>
        </tr>
    </table>
    </fieldset>
</form>

<#if state == "success">
    <script type="text/javascript">
        scsString = "<@spring.message '_mine.user.register.email.has.been.sent' />: <strong>${email}</strong>";
        scsString += "\n\n<@spring.message '_mine.user.register.please.follow.link' />"
        showMessage("success", scsString);
    </script>
    <noscript>
        <p id="forgotSuccess" class="success">
        <@spring.message '_mine.user.register.email.has.been.sent' />: <span>${email}</span>.
        <@spring.message '_mine.user.register.please.follow.link' />.  <!-- TODO change message -->
        </p>
    </noscript>
</#if>
<#if state == "formatFailure">
    <script type="text/javascript">
        failString = "<@spring.message '_mine.user.notification.emailformaterror' />.";
        showMessage("error", failString);
    </script>
    <noscript>
        <@spring.message '_portal.ui.notification.error' />!<br/><@spring.message '_mine.user.notification.emailformaterror' />.
    </noscript>
</#if>
<#if state == "nonexistentFailure">
    <script type="text/javascript">
        failString = "<@spring.message '_portal.ui.notification.error' />!<br/><@spring.message '_mine.user.notification.emailnotregistered' />."
        showMessage("error", failString);
    </script>
    <noscript>
        <@spring.message '_portal.ui.notification.error' />!<br/><@spring.message '_mine.user.notification.emailnotregistered' />. <!-- TODO add message -->
    </noscript>
</#if>

</section>

<@addFooter/>

