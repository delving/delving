<#assign thisPage = "logout.html"/>
<#include "includeMarcos.ftl">

<@addHeader "${portalDisplayName}", "",[],[]/>

<section class="grid_3">
    <header id="branding">
        <a href="/${portalName}/" title=""/>
        <img src="/${portalName}/${portalTheme}/images/norvegiana.jpg" alt="${portalDisplayName}"/>
        </a>
        <h1 class="large">${portalDisplayName}</h1>
    </header>
</section>

<section role="main" class="grid_9">


    <h3>You have successfully logged out</h3>

</section>


<@addFooter/>

