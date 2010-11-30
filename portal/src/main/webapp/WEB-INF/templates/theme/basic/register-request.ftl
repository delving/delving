<#assign thisPage = "register-request.html"/>
<#include "includeMarcos.ftl">

<@addHeader "Norvegiana", "",[],[]/>

<#if contentOnly != "true">
<div class="centered">
    <div class="grid_12" id="branding">
        <h1 class="gigantic">
            <img src="/${portalName}/${portalTheme}/images/norvegiana.jpg" alt="Norvegiana" align="absmiddle"/>${portalDisplayName}
        </h1>
    </div>
</#if>

<section role="main" class="grid_4 prefix_4">

<h2><@spring.message '_register.register' /></h2>

<form id="registrationForm" name='registrationForm' action='register-request.html' method='POST' accept-charset="UTF-8">
    <input type="hidden" name="formType" id="formType" value="Register"/>

    <table>
        <tr>
            <td width="100"><label for="email"><@spring.message '_mine.email.address' /></label></td>
            <td><input id="register_email" type='text' name='email' id="email" value='' accept-charset="UTF-8"></td>
        </tr>
        <tr>
            <td></td>
            <td>
                <input id="register" name="submit_button" type="submit" value="<@spring.message '_register.register' />" class="button"/>
            </td>
        </tr>
    </table>
</form>

<#if state == "success">
    <p id="success" class="success">
        <@spring.message '_register.email.has.been.sent' />: <span class="fg-gold">${email}</span>.
        <@spring.message '_register.please.follow.link' />.
    </p>
</#if>

<#if state == "formatFailure">
    <strong><@spring.message '_portal.ui.notification.error' />: </strong><@spring.message '_messages.error.emailformaterror' />.
</#if>

<#if state == "existenceFailure">
    <strong><@spring.message '_portal.ui.notification.error' />: </strong><@spring.message '_register-request.emailalreadyregistered' />.
</#if>

</section>
</div>
<@addFooter/>

