<#import "spring_form_macros.ftl" as spring />
<#assign thisPage = "logout.html"/>
<#include "inc_header.ftl">

<body>

<div id="sidebar" class="grid_3">

    <div id="identity">
            <h1>Delving</h1>
            <a href="/${portalName}/index.html" title="ABM"><img src="/${portalName}/${portalTheme}/images/abm-logo.jpg" alt="ABM"/></a>
    </div>

</div>

<div id="main" class="grid_9">

    <div id="top-bar">

        <#include "language_select.ftl">
    </div>

    <div class="clear"></div>


    <h3>You have successfully logged out</h3>

</div>


<#include "inc_footer.ftl"/>

</body>
</html>
