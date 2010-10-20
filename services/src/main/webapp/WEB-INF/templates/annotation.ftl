<#if user??>
    <#assign user = user/>
</#if>
<html>
<head>
    <title>Annotation REST Interface</title>
</head>
<body>

<h1>Annotation REST Interface</h1>

<p>
<#if user??>
    <h2>The user "${user.userName?html}" is currently logged in.</h2>
<#else>
    <h2>There is no user logged in. Permission to access will be denied.</h2>
</#if>
</p>

<p>
<table cellpadding="20" border="1">
    <tr>
        <th>Method</th>
        <th>Path</th>
        <th>Function</th>
    </tr>
    <tr>
        <td>GET</td>
        <td>/user/annotation/{type}/{europeanaUri}</td>
        <td>Feth all the annotations related to the given URI</td>
    </tr>
    <tr>
        <td>GET</td>
        <td>/user/annotation/{id}</td>
        <td>Fetch an existing annotation</td>
    </tr>
    <tr>
        <td>POST</td>
        <td>/user/annotation/{type}/{europeanaUri}</td>
        <td>Create a new annotation with no predecessors. Returns ID.</td>
    </tr>
    <tr>
        <td>POST</td>
        <td>/user/annotation/?predecessor=ID</td>
        <td>Create a new annotation with the given predecessor ID. Returns ID.</td>
    </tr>
    <tr>
        <td>PUT</td>
        <td>/user/annotation/{id}</td>
        <td>Update an existing annotation with new content as request body</td>
    </tr>
    <tr>
        <td>DELETE</td>
        <td>/user/annotation/{id}</td>
        <td>Delete an existing annotation</td>
    </tr>
</table>
</p>
</body>
</html>