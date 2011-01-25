<#assign thisPage = "change-password.html"/>
<#assign pageId = "cp"/>
<#include "includeMarcos.ftl">

<@addHeader "${portalDisplayName}", "",[],["login-register.css"]/>

<section role="main" class="grid_12">

    <h1><@spring.message '_mine.forgotpassword' /></h1>

    <form id="regForm" action="change-password.html" method="post">

        <input type="hidden" name="token" value="${command.token}"/>
        <input type="hidden" name="email" value="${command.email}"/><#-- disabled email field below is not submitted so we need this hidden field -->
        <div class="grid_3 alpha">
            <fieldset id="pt1">
                <legend><span>Step </span>1. <span>: Email details</span></legend>
                <label for="email"><@spring.message '_mine.email.address' /></label>
                <input type="text" id="email" name="email" disabled="true" tabindex="5" value="${command.email}"
                       style="background:#eaeaea;"/>

            </fieldset>
        </div>
        <div class="grid_3">
            <fieldset id="pt2">
                <legend><span>Step </span>2. <span>: <@spring.message '_mine.forgotpassword.new.password' /></span>
                </legend>
                <label for="password"><@spring.message '_mine.forgotpassword.new.password' /></label>
                <input type="password" id="password" name="password" tabindex="5" value=""/>
            <@spring.bind "command.password" />
            <#--<#list spring.status.errorMessages as error> <i>${error}</i> <br> </#list>-->
                        <#if spring.status.error>
                            <p class="error"><#list spring.status.errorMessages as error>${error}<br/></#list></p>
                        </#if>
                <label for="password2"><@spring.message '_mine.forgotpassword.new.password.repeat' /></label>
                <input type="password" id="password2" name="password2" tabindex="5" value=""/>
            <@spring.bind "command.password2" />
                        <#if spring.status.error>
                            <p class="error"><#list spring.status.errorMessages as error>${error}<br/></#list></p>
                        </#if>
            </fieldset>
        </div>
        <div class="grid_4 omega">
            <fieldset id="pt3">
                <legend><span>Step </span>3. <span>: Password</span></legend>
                <br/>
                <input id="submit" type="submit" name="submit" tabindex="6"
                       value="<@spring.message '_action.send' /> &raquo;" class="button"/>
            </fieldset>
        </div>
    </form>

</section>

<@addFooter/>

