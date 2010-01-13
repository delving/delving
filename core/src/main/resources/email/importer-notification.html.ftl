<#assign status = status>

<html>

<body>

<p>Hello Human Master,</p>

<p>This email is to inform you of status during import of XML:</p>

<p>Status: ${status}</p>

<#if stackTrace?exists>
<p style="font-family:Courier,Courier New">Stack Trace ====<br />
${stackTrace}</br>
=============</p>
</#if>

<p>- Europeana ESE Importer<br />
eu.europeana.util.ESEImporter</p>

</body>

</html>

