<#assign thisPage = "register-request.html"/>
<#include "includeMarcos.ftl">

<@addHeader "Norvegiana", "",[],[]/>


<div class="centered">
    <div class="grid_12" id="branding">
        <h1 class="gigantic">
            <img src="/${portalName}/${portalTheme}/images/norvegiana.jpg" alt="Norvegiana" align="absmiddle"/>${portalDisplayName}
        </h1>
    </div>


<section role="main" class="grid_4 prefix_4">

<h2><@spring.message 'Register_t' /></h2>

<form id="registrationForm" name='registrationForm' action='register-request.html' method='POST' accept-charset="UTF-8">
    <input type="hidden" name="formType" id="formType" value="Register"/>

    <table>
        <tr>
            <td width="100"><label for="email"><@spring.message 'EmailAddress_t' /></label></td>
            <td><input id="register_email" type='text' name='email' id="email" value='' accept-charset="UTF-8"></td>
        </tr>
        <tr>
            <td></td>
            <td>
                <input id="register" name="submit_button" type="submit" value="<@spring.message 'Register_t' />" class="button"/>
            </td>
        </tr>
    </table>
</form>

<#if state == "success">
    <p id="success" class="success">
        <@spring.message 'AnEmailHasBeenSentTo_t' />: <span class="fg-gold">${email}</span>.
        <@spring.message 'PleaseFollowTheLinkProvided_t' />.
    </p>
</#if>

<#if state == "formatFailure">
    <strong><@spring.message 'Error_t' />: </strong><@spring.message 'EmailFormatError_t' />.
</#if>

<#if state == "existenceFailure">
    <strong><@spring.message 'Error_t' />: </strong><@spring.message 'EmailAlreadyRegistered_t' />.
</#if>

</section>
</div>
<@addFooter/>

