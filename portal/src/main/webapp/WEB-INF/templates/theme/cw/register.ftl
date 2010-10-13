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
<#include "spring_form_macros.ftl"/>
<#include "inc_header.ftl"/>


<div id="main">

    <div class="login-register grid_12">
        
        <h1><@spring.message 'Register_t' /></h1>

        <form id="regForm" action="register.html" method="post">

            <input type="hidden" name="token" value="${command.token}"/>
            <input type="hidden" name="email" value="${command.email}"/>

        <div class="grid_4 alpha">
            <fieldset id="pt1">

                <legend><span>Step </span>1. <span>: Email details</span></legend>
                <h3><@spring.message 'EmailAddress_t' />.</h3>

                <div class="help"><@spring.message 'EmailUse_t' />.</div>
                <table>
                    <tr><td><label for="email"><@spring.message 'EmailAddress_t' /></label></td></tr>
                    <tr><td>
                        <input type="text" id="email" name="email" disabled="true" tabindex="5" value="${command.email}" style="background:#eaeaea;"/>
                    </td></tr>
                </table>

            </fieldset>
        </div>
        <div class="grid_4">
            <fieldset id="pt2">

                <legend><span>Step </span>2. <span>: Login details</span></legend>
                <h3>Gebruikersnaam.</h3>

                <div class="help"><@spring.message 'UserNameExplain_t' />.</div>

                <table>
                    <tr><td><label for="userName">Gebruikersnaam</label></td></tr>
                    <tr><td>
                        <@spring.formInput "command.userName"/>
                        <@spring.bind "command.userName" />
                        <#list spring.status.errorMessages as error><em class="error">${error}</em><br/></#list>
                    </td></tr>
                </table>
                


            </fieldset>
        </div>
        <div class="grid_4 omega">
            <fieldset id="pt3">

                <legend><span>Step </span>3. <span>: Password</span></legend>
                <h3><@spring.message 'PasswordChoose_t' />.</h3>

                <div class="help"><@spring.message 'PasswordExplain_t' />.</div>
                <table>
                    <tr><td><label for="password"><@spring.message 'Password_t' /></label></td></tr>
                    <tr><td>                            
                        <@spring.formPasswordInput "command.password"/>
                        <@spring.bind "command.password" />
                        <#list spring.status.errorMessages as error><em class="error">${error}</em><br/></#list>
                    </td>
                    </tr>
                    <tr><td><label for="password2"><@spring.message 'RepeatPassword_t' /></label></td></tr>
                    <tr><td>
                        <@spring.formPasswordInput "command.password2"/>
                        <@spring.bind "command.password2" />
                        <#list spring.status.errorMessages as error><em class="error">${error}</em><br/></#list>
                    </td>
                    </tr>
                </table>





            </fieldset>
         </div>
         <div class="clear"></div>


            <fieldset id="pt4">
                <legend>Step 4 : Submit form</legend>
                <h3>&#160;</h3>

                <div id="disclaimer-text">
                    <@spring.message 'MyCodeOfConduct_t' />
                </div>


     
                <table>
                    <tr>
                        <td>
                            <@formCheckbox "command.disclaimer"/>
                            <#list spring.status.errorMessages as error><em class="error">${error}</em><br/> </#list>
                        </td>
                    </tr>
                    <tr>
                        <td><input id="submit_registration" type="submit" name="submit_registration" tabindex="6" value="<@spring.message 'FinishRegistration_t' /> &raquo;" class="button"/></td>
                    </tr>
                </table>




            </fieldset>

        </form>
    </div>
<#include "inc_footer.ftl"/>
</div>
<#macro formCheckbox path attributes="">
<@spring.bind path />
    <#assign id="${spring.status.expression}">
    <#assign status="${spring.stringStatusValue}">
<div id="agree">
    <input type="hidden" name="_${id}" value="false"/>
    <input type="checkbox" id="${id}" name="${id}"
<#--<#if spring.status.value>checked="checked"</#if>-->
${attributes}
<@spring.closeTag/>
<@spring.message 'IAgree_t'/>
</div>
</#macro>