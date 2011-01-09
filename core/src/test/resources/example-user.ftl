<#assign user = user>

First the basics:
        first        : ${user.firstName}
        last         : ${user.lastName}
        username     : ${user.userName}
        email        : ${user.email}
        newsletter?  : <#if user.newsletter>Newsletter<#else>No Newsletter</#if>
        registration : ${user.registrationDate}
        last login   : ${user.lastLogin}

Saved Searches:
<#list user.searches as search>
    -------------------
    query : ${search.query}
    saved : ${search.dateSaved}
</#list>

Saved Items:
<#list user.items as doc>
    -------------------
    title            : ${doc.title}
    author           : ${doc.author}
    saved            : ${doc.dateSaved}
    europeana uri    : ${doc.europeanaId.europeanaUri}
    europeana object : ${doc.europeanaObject}
</#list>

