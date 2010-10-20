<#import "spring.ftl" as spring />
<#assign thisPage = "register-success.html">
<#include "spring_form_macros.ftl"/>
<#assign pageId = "rsp">
<#include "inc_header.ftl"/>


<div id="main">

    <div class="login-register grid_5">
        <h1 id="register_success"><@spring.message "registrationSucceed_t" /></h1>

        <!--<p>You are now a culture vulture.</p>-->

        <form id="loginForm" name='f' action='j_spring_security_check' method='POST'>
        <#-- _spring_security_remember_me should always be true.
 The user shouldn't have an option to uncheck it.
 That's why this is a hidden field and not a checkbox -->
            <input class="inline" type='hidden' value="true" id='_spring_security_remember_me' name='_spring_security_remember_me'/>

            <fieldset>
            <table>
                <tr>
                    <td><label for="j_username">Username</label></td>
                    <td><input type="text" id="j_username" name="j_username" value="${command.email}"/></td>
                </tr>
                <tr>
                    <td><label for="j_password"><@spring.message 'Password_t' /></label></td>
                    <td><input type="password" id="j_password" name="j_password" value="${command.password}"/></td>
                </tr>
                <tr>
                    <td>&#160;</td>
                    <td><input name="submit_login" type="submit" value="Login" class="button"/></td>
                </tr>
            </table>








            </fieldset>
        </form>

    </div>
</div>
<#include "inc_footer.ftl"/>

