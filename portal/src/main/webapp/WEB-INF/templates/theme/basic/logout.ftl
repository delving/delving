<#import "spring_form_macros.ftl" as spring />
<#assign thisPage = "logout.html"/>
<#include "delving-macros.ftl">

<@addHeader "Norvegiana", "",[],[]/>

<section class="grid_3">
    <header id="branding">
        <a href="/${portalName}/" title=""/>
        <img src="/${portalName}/${portalTheme}/images/norvegiana.jpg" alt="Norvegiana"/>
        </a>
        <h1 class="large">${portalDisplayName}</h1>
    </header>
</section>

<section role="main" class="grid_9">


    <h3>You have successfully logged out</h3>

</section>


<#include "inc_footer.ftl"/>

