<#import "/spring.ftl" as spring />
<#assign thisPage = "forgotPassword.html"/>
<#assign pageId = "fp"/>
<#include "inc_header.ftl"/>

<div id="doc4" class="yui-t2">
    <div id="hd">
        <#include "inc_top_nav.ftl"/>
    </div>
   <div id="bd">
    <div id="yui-main">
        <div class="yui-b">
            <div class="yui-g" id="mainContent">

                <h1><@spring.message 'ForgotPassword_t' /></h1>

                <form id="regForm" action="change-password.html" method="post">

                    <input type="hidden" name="token" value="${command.token}" />
                    <input type="hidden" name="email" value="${command.email}" /><#-- disabled email field below is not submitted so we need this hidden field -->

                    <fieldset id="pt1">
                        <legend><span>Step </span>1. <span>: Email details</span> </legend>
                        <label for="email"><@spring.message 'EmailAddress_t' /></label>
                        <input type="text" id="email" name="email" disabled="true" tabindex="5"  value="${command.email}" style="background:#eaeaea;"/>

                    </fieldset>

                    <fieldset id="pt2">
                        <legend><span>Step </span>2. <span>: Password</span></legend>
                        <label for="password"><@spring.message 'Password_t' /></label>
                        <input type="password" id="password" name="password" tabindex="5"  value=""/>
                        <@spring.bind "command.password" />
                        <#list spring.status.errorMessages as error> <i>${error}</i> <br> </#list>

                        <label for="password2"><@spring.message 'RepeatPassword_t' /></label>
                        <input type="password" id="password2" name="password2" tabindex="5"  value=""/>
                        <@spring.bind "command.password2" />
                        <#list spring.status.errorMessages as error> <i>${error}</i> <br> </#list>
                    </fieldset>

                    <fieldset id="pt3">
                      <legend><span>Step </span>3. <span>: Password</span></legend>
                      <br/>
                      <input id="submit" type="submit" name="submit" tabindex="6" value="<@spring.message 'Send_t' /> &raquo;" class="button"/>
                    </fieldset>

              </form>

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

