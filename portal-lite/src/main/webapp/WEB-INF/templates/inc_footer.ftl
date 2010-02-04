<#--<#assign TermsConditionsCurrent = ""/>
<#assign LanguagePolicyCurrent = ""/>
<#assign PrivacyCurrent = ""/>
<#assign AccessibilityCurrent = ""/>
<#assign AboutUsCurrent = ""/>
<#assign ContactCurrent = ""/>
<#assign UsingEuropeanaCurrent = ""/>
<#assign SiteMapCurrent = ""/>
<#if thisPage?exists>
    <#switch thisPage>
        <#case "termsofservice.html">
        <#assign TermsConditionsCurrent = "current"/>
            <#break>
        <#case "languagepolicy.html">
        <#assign LanguagePolicyCurrent = "current"/>
            <#break>
        <#case "privacy.html">
        <#assign PrivacyCurrent = "current"/>
            <#break>
        <#case "accessibility.html">
            <#assign AccessibilityCurrent = "current"/>
        <#break>
        <#case "aboutus.html">
            <#assign AboutUsCurrent = "current"/>
        <#break>
        <#case "contact.html">
            <#assign ContactCurrent = "current"/>
        <#break>
        <#case "usingeuropeana.html">
            <#assign UsingEuropeanaCurrent = "current"/>
        <#break>
        <#case "sitemap.html">
            <#assign SiteMapCurrent = "current"/>
        <#break>
        <#default>
            <#break>
    </#switch>
</#if>-->
<div class="grid_12" id="footer">
<br/><br/>
<#--<ul>
  <li><a href="using-europeana.html" class="${UsingEuropeanaCurrent}"><@spring.message 'UsingEuropeana_t' /></a></li>
  <li><a href="accessibility.html" class="${AccessibilityCurrent}"><@spring.message 'Accessibility_t' /></a></li>
  <li><a href="termsofservice.html" class="${TermsConditionsCurrent}"><@spring.message 'TermsAndConditions_t' /></a></li>
  <li><a href="privacy.html" class="${PrivacyCurrent}"><@spring.message 'Privacy_t' /></a></li>
  <li><a href="languagepolicy.html" class="${LanguagePolicyCurrent}"><@spring.message 'LanguagePolicy_t' /></a></li>
  <li><a href="contact.html" class="${ContactCurrent}"><span  class="fg-red"><@spring.message 'Contacts_t' /> | <@spring.message 'SendUsFeedback_t' /></span></a></li>
  <li class="signoff"><@spring.message 'FundedBy_t' /><img src="images/eu-flag.gif" alt="icon-european union flag"/></li>
</ul>-->

</div><!-- end footer -->
</div><!-- end container_12 -->

</body>
</html>
