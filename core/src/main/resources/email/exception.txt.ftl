<#assign stackTrace = stackTrace>
<#assign request = request>
<#assign hostName = hostName>
<#assign agent = agent>
<#assign referer = referer>

Hello Human Master,

This email is to inform you that something went wrong:

Requested Url: ${request} on host ${hostName}.

Requested by ${agent} from referer page ${referer}

==========
${stackTrace}
==========

- Europeana Exception Resolver
eu.europeana.web.util.ExceptionResolver

