<#assign thisPage = "register-success.html">

<#assign pageId = "rsp">
<#include "includeMarcos.ftl">

<@addHeader "${portalDisplayName}", "",[],[]/>

<section role="main" class="grid_4 prefix_4">

    <h2 id="register_success"><@spring.message "_mine.user.registration.notification.succeed" /></h2>

     <form id="loginForm" name='f' action='j_spring_security_check' method='POST'>
          <#-- _spring_security_remember_me should always be true.
             The user shouldn't have an option to uncheck it.
             That's why this is a hidden field and not a checkbox -->
            <input type='hidden' value="true" id='_spring_security_remember_me' name='_spring_security_remember_me'/>
              <fieldset>
                  <legend><@spring.message '_mine.login' /></legend>
              <table>
             <tr>
                 <td><label for="j_username"><@spring.message '_mine.email.address' /></label></td>
                 <td><input type="text" id="j_username" name="j_username" value="${command.email}"/></td>
             </tr>
             <tr>
                 <td><label for="j_password"><@spring.message '_mine.user.register.password' /></label></td>
                 <td> <input type="password" id="j_password" name="j_password" value="${command.password}"/></td>
             </tr>
             <tr>
                 <td></td>
                 <td><input name="submit_login" type="submit" value="Login" class="button"/></td>
             </tr>
         </table>
         </fieldset>
    </form>

</section>

<script type="text/javascript">
    <#-- nullify takeMeBack cookie so that user is not returned to registration page -->
    $.cookie('takeMeBack', null);
</script>

<@addFooter/>

