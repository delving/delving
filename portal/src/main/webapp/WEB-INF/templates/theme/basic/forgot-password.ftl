<#import "spring.ftl" as spring />
<#assign thisPage = "forgot-password.html"/>

<#include "includeMarcos.ftl">

<@addHeader "${portalDisplayName}", "",[],[]/>

<div class="centered">
    <div class="grid_12" id="branding">
        <h1 class="gigantic">
            <img src="/${portalName}/${portalTheme}/images/norvegiana.jpg" alt="${portalDisplayName}" align="absmiddle"/>${portalDisplayName}
        </h1>
    </div>


<section role="main" class="grid_4 prefix_4">

<h2>
    <@spring.message '_mine.forgotpassword' />
</h2>

<form action="forgot-password.html" method="POST" accept-charset="UTF-8">
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
</form>

<#if state == "success">
<p id="forgotSuccess" class="success">
<@spring.message '_mine.user.register.email.has.been.sent' />: <span class="fg-gold">${email}</span>.
<@spring.message '_mine.user.register.please.follow.link' />.  <!-- TODO change message -->
</p>
</#if>
<#if state == "formatFailure">
<@spring.message '_portal.ui.notification.error' />!<br/><@spring.message '_mine.user.notification.emailformaterror' />.
</#if>
<#if state == "nonexistentFailure">
<@spring.message '_portal.ui.notification.error' />!<br/>EmailDoesntExist_t <!-- TODO add message -->
</#if>

</section>

</div>
<@addFooter/>

