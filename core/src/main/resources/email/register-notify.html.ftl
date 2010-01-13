<#assign user = user>

<html>

<body>

<p>Hello Human Gatekeeper,</p>

<p>This email is to inform you that someone has registered:</p>

<p style="font-family:Courier,Courier New">==========<br/>
email        : ${user.email}<br/>
registration : ${user.registrationDate?date}<br/>
==========</p>

<p>- Europeana Registration Machine<br />
RegisterController</p>

</body>

</html>