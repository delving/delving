<#import "spring.ftl" as spring />
<#assign thisPage = "register.html">
<#assign  pId = "reg">
<#assign view = "table"/>

<#assign query = ""/>
<#if RequestParameters.view?exists>
    <#assign view = "${RequestParameters.view}"/>
</#if>
<#if RequestParameters.query?exists>
    <#assign query = "${RequestParameters.query}"/>
</#if>
<#include "includeMarcos.ftl">

<@addHeader "Norvegiana", "",[],["login-register.css"]/>


    <div class="grid_12" id="branding">
        <h1 class="gigantic">
            <img src="/${portalName}/${portalTheme}/images/norvegiana.jpg" alt="Norvegiana" align="absmiddle"/>${portalDisplayName}
        </h1>
    </div>


<section role="main" class="grid_12 login-register">


                <h1><@spring.message 'Register_t' /></h1>


                <form id="regForm" action="/${portalName}/register.html" method="post">

                    <input type="hidden" name="token" value="${command.token}" />
                    <input type="hidden" name="email" value="${command.email}" />

                    <div class="grid_3 alha">
                    <fieldset id="pt1">

                        <legend><span>Step </span>1. <span>: Email details</span> </legend>
                        <h3><@spring.message '_prompt.email.address' />.</h3>
                        <div class="help"><@spring.message 'EmailUse_t' />.</div>
                        <!--<strong class="error">An email address is required!</strong>-->
                        <label for="email"><@spring.message '_prompt.email.address' /></label>
                        <input type="text" id="email" name="email" disabled="true" tabindex="5"  value="${command.email}" style="background:#eaeaea;"/>

                    </fieldset>
                    </div>

                    <div class="grid_3">
                    <fieldset id="pt2">


                        <legend><span>Step </span>2. <span>: Login details</span></legend>
                        <h3><@spring.message 'UserNameChoose_t' />.</h3>
                        <div class="help"><@spring.message 'UserNameExplain_t' />.</div>
                        <label for="userName">Username</label>
                        <@spring.formInput "command.userName"/>
                        <@spring.bind "command.userName" />
                        <#list spring.status.errorMessages as error> <i>${error}</i> <br> </#list>

                    </fieldset>
                    </div>

                    <div class="grid_3">
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
                    </div>

                    <div class="grid_3 omega">
                    <fieldset id="pt4">
                        <legend><span>Step </span>4. <span>: Submit form</span></legend>
                        <#--<h3>Agree</h3>-->
                        <#-- todo: rewrite this with proper spring bindings -->
                        <#--<label for="disclaimer"><@spring.message 'MyCodeOfConduct_t' /></label>-->
                        <div id="disclaimer-texts" class="help"><@spring.message 'MyCodeOfConduct_t' /></div>
                        <p>
                        <@formCheckbox "command.disclaimer"/>
                        <br />
                        <#list spring.status.errorMessages as error> <i>${error}</i> <br> </#list>

                        <input id="submit_registration" type="submit" name="submit_registration" tabindex="6" value="<@spring.message 'FinishRegistration_t' /> &raquo;" class="button"/>
                        </p>
                    </fieldset>
                    </div>
                    
                </form>

</section>

	    <@addFooter/>

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
