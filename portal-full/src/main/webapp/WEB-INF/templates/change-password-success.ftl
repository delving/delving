<#import "spring.ftl" as spring />
<#assign thisPage = "register-success.html">
<#assign view = "table"/>
<#assign query = ""/>
<#if RequestParameters.view?exists>
    <#assign view = "${RequestParameters.view}"/>
</#if>
<#if RequestParameters.query?exists>
    <#assign query = "${RequestParameters.query}"/>
</#if>
<#include "spring_form_macros.ftl"/>
<#include "inc_header.ftl"/>
<div id="doc4" class="yui-t2">
    <div id="hd">
        <#include "inc_top_nav.ftl"/>
    </div>
   <div id="bd">
    <div id="yui-main">
        <div class="yui-b">
            <div class="yui-g" id="mainContent">
                <div class="yui-u first">

                <h1 id="register_success"><@spring.message "registrationSucceed_t" /></h1>

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
            </div>
        </div>
    </div>
        <div class="yui-b">
            <#include "inc_logo_sidebar.ftl"/>
        </div>
    </div>
   <div id="ft">
	    <#include "inc_footer.ftl"/>
   </div>
</div>
</body>
</html>
