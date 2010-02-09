<#import "spring.ftl" as spring />
<#assign thisPage = "sitemap.html">
<#assign view = "table"/>
<#assign query = ""/>
<#if RequestParameters.view?exists>
    <#assign view = "${RequestParameters.view}"/>
</#if>
<#if RequestParameters.query?exists>
    <#assign query = "${RequestParameters.query}"/>
</#if>
<#include "inc_header.ftl"/>
<div id="doc4" class="yui-t2">
    <div id="hd">
    <#include "inc_top_nav.ftl"/>
    </div>
    <div id="bd">
        <div id="yui-main">
            <div class="yui-b">
                <div class="yui-g" id="mainContent">
                    <h1><@spring.message 'Sitemap_t' /></h1>


                    <div id="secondaryContent">

                        <div class="yui-u first">
                            <ul class="share-ideas">
                                <li><a href="myeuropeana.html"><@spring.message 'MyEuropeana_t' /></a></li>
                                <li><a href="communities.html"><@spring.message 'Communities_t' /></a></li>
                                <li><a href="partners.html"><@spring.message 'Partners_t' /></a></li>
                                <li><a href="year-grid.html"><@spring.message 'Timeline_t' /></a></li>
                                <li><a href="though-lab.html"><@spring.message 'ThoughtLab_t' /></a></li>

                            </ul>
                        </div>
                        <div class="yui-u">
                            <ul class="people-talk">
                                <li><a href="aboutus.html"><@spring.message 'AboutUs_t' /></a>
                                    <ul class="sub">
                                        <li><a href="aboutus.html#background">Background</a></li>
                                        <li><a href="aboutus.html#technicalplans">Technical plans</a></li>
                                        <li><a href="aboutus.html#contribute">Contribute content</a></li>
                                    </ul>
                                </li>
                                <li><a href="usingeuropeana.html"><@spring.message 'UsingEuropeana_t' /></a></li>
                                <li><a href="accessibility.html"><@spring.message 'Accessibility_t' /></a></li>
                                <li><a href="sitemap.html"><@spring.message 'Sitemap_t' /></a></li>
                                <li><a href="termsofservice.html"><@spring.message 'TermsAndConditions_t' /></a>
                                <#-- <ul class="sub">
                                    <li><a href="termsofservice.html">Copyright</a></li>
                                    <li><a href="termsofservice.html">Permitted use</a></li>
                                </ul>-->
                                </li>
                                <li><a href="privacy.html"><@spring.message 'Privacy_t' /></a></li>
                                <li><a href="languagepolicy.html"><@spring.message 'LanguagePolicy_t' /></a></li>
                                <li><a href="contact.html"><@spring.message 'Contacts_t' /></a></li>
                            </ul>
                        </div>

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
