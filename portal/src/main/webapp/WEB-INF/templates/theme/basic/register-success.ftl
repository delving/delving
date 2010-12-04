<#assign thisPage = "register-success.html">

<#assign pageId = "rsp">
<#include "includeMarcos.ftl">

<@addHeader "${portalDisplayName}", "",[],[]/>

<section role="main" class="grid_4 prefix_4">

    <h1 id="register_success"><@spring.message "_mine.user.registration.notification.succeed" /></h1>

    <!--<p>You are now a culture vulture.</p>-->

     <form id="loginForm" name='f' action='j_spring_security_check' method='POST'>
     <fieldset>
        <#--<input type='hidden' name='j_username' value='${emailAddress}'>-->
        <#--<input type='hidden' name='j_password' value='${password}'/>-->
         <label for="j_username">Username</label>
         <input type="text" id="j_username" name="j_username" value="${command.email}"/>

         <label for="j_password"><@spring.message '_mine.user.register.password' /></label>
         <input type="password" id="j_password" name="j_password" value="${command.password}"/>

         <#-- _spring_security_remember_me should always be true.
             The user shouldn't have an option to uncheck it.
             That's why this is a hidden field and not a checkbox -->
         <input class="inline" type='hidden' value="true" id='_spring_security_remember_me'
                name='_spring_security_remember_me'/>

        <input name="submit_login" type="submit" value="Login" class="button"/>
     </fieldset>
    </form>
</section>

<@addFooter/>

