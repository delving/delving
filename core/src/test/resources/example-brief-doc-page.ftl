<#assign model = result>

Query duration was ${model.queryDuration}.

Here come the docs:

<#list model.briefDocWindow.docs as doc>
    DOCUMENT:
        ID          : ${doc.id}
        TITLE       : ${doc.title}
        THUMBNAIL   : ${doc.thumbnail}
        YEAR        : ${doc.year}
        CREATOR     : ${doc.creator}
        LANGUAGE    : ${doc.language}
        TYPE        : ${doc.type}
</#list>

Now the dc_date facet:

<#list model.facets as facet>
        ${facet.type}
        <#list facet.counts as count>
            ${count.value} : ${count.count}
        </#list>
</#list>
