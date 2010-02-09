<#import "spring.ftl" as spring />
<#assign thisPage = "register-success.html">
<#include "spring_form_macros.ftl"/>
<#include "inc_header.ftl"/>
<#assign pageId = "rsp">
<div id="sidebar" class="grid_3">

    <div id="identity">
            <h1>Europeana Lite</h1>
            <a href="index.html" title="Europeana lite"><img src="images/europeana_open_logo_small.jpg" alt="European Open Source"/></a>
    </div>

</div>

<div id="main" class="grid_9">

    <div id="top-bar">
        <@userbar/>
        <#include "language_select.ftl">
    </div>

    <div class="clear"></div>

    <h1 id="register_success"><@spring.message "registrationSucceed_t" /></h1>

    <!--<p>You are now a culture vulture.</p>-->

     <form id="loginForm" name='f' action='j_spring_security_check' method='POST'>
     <fieldset>
        <#--<input type='hidden' name='j_username' value='${emailAddress}'>-->
        <#--<input type='hidden' name='j_password' value='${password}'/>-->
         <label for="j_username">Username</label>
         <input type="text" id="j_username" name="j_username" value="${command.email}"/>

         <label for="j_password"><@spring.message 'Password_t' /></label>
         <input type="password" id="j_password" name="j_password" value="${command.password}"/>

         <#-- _spring_security_remember_me should always be true.
             The user shouldn't have an option to uncheck it.
             That's why this is a hidden field and not a checkbox -->
         <input class="inline" type='hidden' value="true" id='_spring_security_remember_me'
                name='_spring_security_remember_me'/>

        <input name="submit_login" type="submit" value="Login" class="button"/>
     </fieldset>
    </form>
</div>
	    <#include "inc_footer.ftl"/>

