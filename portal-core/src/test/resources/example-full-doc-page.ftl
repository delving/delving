<#assign model = result>

Query duration was ${model.queryDuration}.

Here comes the doc:

 FULL DOCUMENT:
        ID          : ${model.fullDoc.id}
        TITLE       : ${model.fullDoc.title}

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
