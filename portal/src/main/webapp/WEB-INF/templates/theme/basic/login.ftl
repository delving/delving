<#assign thisPage = "login.html"/>
<#--<#assign register = register>-->

<#include "includeMarcos.ftl">

<@addHeader "Norvegiana", "",[],[]/>

    <div class="grid_12" id="branding">
        <h1 class="gigantic">
            <img src="/${portalName}/${portalTheme}/images/norvegiana.jpg" alt="Norvegiana" align="absmiddle"/>${portalDisplayName}
        </h1>
    </div>


<section role="main" class="grid_4 prefix_4">
<h2><@spring.message 'LogIn_t' /></h2>

<form name='f1' id="loginForm" action='j_spring_security_check' method='POST' accept-charset="UTF-8">
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
            <a href="/${portalName}/forgot-password.html"><@spring.message 'ForgotPassword_t' /></a>
        </td>
        <td align="right"><input name="submit_login" type="submit" value="<@spring.message 'LogIn_t' />"/></td>
    </tr>
</table>
<#if errorMessage>

<strong><@spring.message 'Error_t' />: </strong> Inlog gegevens zijn niet juist

</#if>
</section>

<@addFooter/>

