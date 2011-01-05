<#assign user = user>

Hello Human Gatekeeper,

This email is to inform you that someone has registered:

==========
email        : ${user.email}
username     : ${user.userName}
registration : ${user.registrationDate?date}
==========

- Europeana Registration Machine
eu.europeana.controller.RegisterController

