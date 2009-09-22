<#import "spring.ftl" as spring />
<#assign thisPage = "register.html">
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

                <h1><@spring.message 'Register_t' /></h1>

                <form id="regForm" action="register.html" method="post">

                    <input type="hidden" name="token" value="${command.token}" />
                    <input type="hidden" name="email" value="${command.email}" />

                    <fieldset id="pt1">

                        <legend><span>Step </span>1. <span>: Email details</span> </legend>
                        <h3><@spring.message 'EmailAddress_t' />.</h3>
                        <div class="help"><@spring.message 'EmailUse_t' />.</div>
                        <!--<strong class="error">An email address is required!</strong>-->
                        <label for="email"><@spring.message 'EmailAddress_t' /></label>
                        <input type="text" id="email" name="email" disabled="true" tabindex="5"  value="${command.email}" style="background:#eaeaea;"/>

                    </fieldset>
                    <fieldset id="pt2">


                        <legend><span>Step </span>2. <span>: Login details</span></legend>
                        <h3><@spring.message 'UserNameChoose_t' />.</h3>
                        <div class="help"><@spring.message 'UserNameExplain_t' />.</div>
                        <label for="userName">Username</label>
                        <@spring.formInput "command.userName"/>
                        <@spring.bind "command.userName" />
                        <#list spring.status.errorMessages as error> <i>${error}</i> <br> </#list>

                    </fieldset>
                    <fieldset id="pt3">

                        <legend><span>Step </span>3. <span>: Password</span></legend>
                        <h3><@spring.message 'PasswordChoose_t' />.</h3>
                        <div class="help"><@spring.message 'PasswordExplain_t' />.</div>

                        <label for="password"><@spring.message 'Password_t' /></label>
                        <@spring.formPasswordInput "command.password"/>
                        <@spring.bind "command.password" />
                        <#list spring.status.errorMessages as error> <i>${error}</i> <br> </#list>

                        <label for="password2"><@spring.message 'RepeatPassword_t' /></label>
                        <@spring.formPasswordInput "command.password2"/>
                        <@spring.bind "command.password2" />
                        <#list spring.status.errorMessages as error> <i>${error}</i> <br> </#list>

                    </fieldset>
                    <fieldset id="pt4">
                        <legend>Step 4  : Submit form</legend>
                        <h3>&#160;</h3>
                         <style>
                             #agree {
                                 font-size: 1em;
                                 text-align: left;
                             }
                             #agree #disclaimer {
                                 border: 1px solid green;
                                 text-align: left;
                                 float: left;
                             }
                         </style>
                        <#-- todo: rewrite this with proper spring bindings -->
                        <#--<label for="disclaimer"><@spring.message 'MyCodeOfConduct_t' /></label>-->
                        <div id="disclaimer_text"><@spring.message 'MyCodeOfConduct_t' /></div>
                        <@formCheckbox "command.disclaimer"/>
                        <#list spring.status.errorMessages as error> <i>${error}</i> <br> </#list>


                        <#-- todo remove old checkbox-->
                        <#--<div id="disclaimer"><@spring.message 'MyCodeOfConduct_t' /></div>-->
                        <#--<div id="agree"><input type="checkbox" value="1" name="agreeDisclaimer"/><@spring.message 'IAgree_t' /></div>-->

                        <input id="submit_registration" type="submit" name="submit_registration" tabindex="6" value="<@spring.message 'FinishRegistration_t' /> &raquo;" class="button"/>
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

<#macro formCheckbox path attributes="">
    <@spring.bind path />
    <#assign id="${spring.status.expression}">
    <#assign status="${spring.stringStatusValue}">
    <div id="agree">
    <input type="hidden" name="_${id}" value="false" />
    <input type="checkbox" id="${id}" name="${id}"
    <#--<#if spring.status.value>checked="checked"</#if>-->
    ${attributes}
    <@spring.closeTag/>
    <@spring.message 'IAgree_t'/>
    </div>
</#macro>