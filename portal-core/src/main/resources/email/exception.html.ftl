<#assign stackTrace = stackTrace>
<#assign request = request>
<#assign hostName = hostName>

<html>

<body>

<p>Hello Human Master,</p>

<p>This email is to inform you that something went wrong:<br/>
<br/>
Requested Url: <a href="${request}">${request}</a> on host <a href="${hostName}">${hostName}</a>.</p>

<p style="font-family:Courier,Courier New">==========<br />
${stackTrace}<br />
==========</p>

<p>- Europeana Exception Resolver<br />
eu.europeana.controller.util.ExceptionResolver</p>

</body>

</html>