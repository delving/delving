<#assign result = result/>
{"ids":[
<#list result.docIdWindow.ids as id>
"${id}",
</#list>
]}