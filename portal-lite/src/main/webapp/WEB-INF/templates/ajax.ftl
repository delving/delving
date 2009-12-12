<#assign success = success/>
<#assign exception = exception/>
<#assign debug = debug/>
<?xml version="1.0"?>
<reply>
<success>${success}</success>
<#if debug>
    <exception>
        ${exception}
    </exception>
</#if>
</reply>