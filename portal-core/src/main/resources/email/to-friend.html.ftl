<#assign user = user>
<#assign uri = uri>

<html>

<body>

<p>Hello ${email},</p>

<p>Here is a link to a Europeana cultural treasure:<br />
<a href="${uri}">${uri}</a></p>

<p>Sent by ${user.email}</p>

</body>

</html>