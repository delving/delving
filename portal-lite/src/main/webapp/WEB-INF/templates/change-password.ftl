<#import "spring.ftl" as spring />
<#assign thisPage = "change-password.html"/>
<#assign pageId = "cp"/>
<#include "inc_header.ftl"/>
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

</body>
</html>

