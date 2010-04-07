<#assign stackTrace = stackTrace>
<#assign request = request>
<#assign hostName = hostName>
<#assign agent = agent>
<#if referer??>
    <#assign referer = referer />
<#else>
    <#assign referer = "(no referrer)" />    
</#if>

Hello Human Master,

This email is to inform you that something went wrong:

Requested Url: ${request} on host ${hostName}.

Requested by ${agent} from referer page ${referer}

==========
${stackTrace}
==========

- Europeana Exception Resolver
eu.europeana.web.util.ExceptionResolver

