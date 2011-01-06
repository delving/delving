<#assign thisPage = "login.html"/>
<#--<#assign register = register>-->

<#include "includeMarcos.ftl">

<@addHeader "${portalDisplayName}", "",[],[]/>


<section role="main" class="grid_5 prefix_4">
<h2><@spring.message '_mine.login' /></h2>

<form name='f1' id="loginForm" action='j_spring_security_check' method='POST' accept-charset="UTF-8">
<#if contentOnly = "true"><input type="hidden" name="ajax" value="true"/></#if>    
<table>
    <tr>
        <td><label for="j_username"><@spring.message '_mine.email.address' /></label></td>
        <td><input type='text' id="j_username" name="j_username" value="" maxlength="50"></td>
    </tr>
    <tr>
        <td><label for="j_password"><@spring.message "_mine.user.register.password" /></label></td>
        <td><input type='password' id="j_password" name='j_password' maxlength="50"/></td>
    </tr>
    <tr>
        <td></td>
        <td><input type="checkbox" checked="checked" name="_spring_security_remember_me" id="_spring_security_remember_me" value="true"/>&#160;<label for="_spring_security_remember_me">Remember me</label> </td>
    </tr>
    <tr>
        <td>
            <a href="/${portalName}/forgot-password.html"><@spring.message '_mine.forgotpassword' /></a>
        </td>
        <td align="right"><input name="submit_login" class="button" type="submit" value="<@spring.message '_mine.login' />"/></td>
    </tr>
</table>
<div id="login-err-msg"></div>

<#if errorMessage>
    <script type="text/javascript">
        showMessage("error","<strong><@spring.message '_portal.ui.notification.error' />: </strong><@spring.message '_mine.user.notification.login.failed'/>")
    </script>
    <noscript>
        <strong><@spring.message '_portal.ui.notification.error' />: </strong><@spring.message '_mine.user.notification.login.failed'/>    
    </noscript>
<strong>

</#if>
</section>
<#if contentOnly = "true">
<script type="text/javascript">

        $("form#loginForm").submit(function(){
            $.ajax({
                url: "j_spring_security_check",
                type: "POST",
                data: $("#loginForm").serialize(),

                success: function(result) {
                    if (result == "fail") {
                       $('div#login-err-msg').html('<@spring.message '_mine.user.notification.login.failed' />')
                    }
                    else {
                       document.location.href="index.html";
                    }
                }
            });

            return false;
        })

</script>
</#if>
<@addFooter/>

