<#assign thisPage = "register-request.html"/>
<#include "includeMarcos.ftl">

<@addHeader "${portalDisplayName}", "",[],[]/>

<section role="main" class="grid_4 prefix_4">

<form id="registrationForm" name='registrationForm' action='register-request.html' method='POST' accept-charset="UTF-8" >
    <input type="hidden" name="formType" id="formType" value="Register"/>
    <fieldset class="ui-widget ui-widget-content ui-corner-all">
        <legend><@spring.message '_mine.user.register.register' /></legend>
    <table>
        <tr>
            <td width="100"><label for="email"><@spring.message '_mine.email.address' /></label></td>
            <td><input type='text' name='email' id="email" value='' accept-charset="UTF-8"></td>
        </tr>
        <tr>
            <td></td>
            <td>
                <input id="register" name="submit_button" type="submit" value="<@spring.message '_mine.user.register.register' />" class="button"/>
            </td>
        </tr>
    </table>
   </fieldset>
</form>

<#if state == "success">
    <script type="text/javascript">
          showMessage("success","<@spring.message '_mine.user.register.email.has.been.sent' />: <strong>${email}</strong>. "+"<br/><@spring.message '_mine.user.register.please.follow.link' />")
    </script>
    <noscript>
        <p id="success" class="success">
            <@spring.message '_mine.user.register.email.has.been.sent' />: {email}.
            <@spring.message '_mine.user.register.please.follow.link' />.
        </p>
    </noscript>
</#if>

<#if state == "formatFailure">
    <script type="text/javascript">
          showMessage("error","<@spring.message '_portal.ui.notification.error' />: </strong><@spring.message '_mine.user.notification.emailformaterror' />")
    </script>
    <noscript>
        <strong><@spring.message '_portal.ui.notification.error' />: </strong><@spring.message '_mine.user.notification.emailformaterror' />.
    </noscript>
</#if>

<#if state == "existenceFailure">
    <script type="text/javascript">
          showMessage("error","<@spring.message '_portal.ui.notification.error' />: </strong><@spring.message '_mine.user.register-request.emailalreadyregistered' />")
    </script>
    <noscript>
        <strong><@spring.message '_portal.ui.notification.error' />: </strong><@spring.message '_mine.user.register-request.emailalreadyregistered' />.
    </noscript>
</#if>

</section>
<script type="text/javascript">
    <#-- nullify takeMeBack cookie so that user is not returned to registration page -->
    $.cookie('takeMeBack', null);
</script>
<@addFooter/>

