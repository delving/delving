<#assign nav = "search">
<#assign subnav = "history">
<#assign page = "search_history">
<#include "/header.ftl">

<div id="mainContent" class="fullwidth">

<h2 class="fgRed"><@spring.message 'history_t' /></h2>

				<table class="formtable" cellpadding="4" cellspacing="1" width="100%">
				<tr>
					<th colspan="4" class="th_grey"><@spring.message 'sessionHistory_t' /></th>
				</tr>
<#if johnny?exists>
    <#assign nbHist=quer?size+1>

   <#list quer as qq>
        <form method="POST" style="margin:0;">
				<#assign nbHist=nbHist-1>
				<tr>
					<td>${nbHist}</td>
					<td><input type="text" value='${qq[0]}' style="width:200px" /></td>
					<td><input type="hidden" name="fullUrl" value='${qq[1]}' />
						<select name="hist" style="width:300px">
								<option value="all">all selection</option>
								<#if johnny[qq[0]]?exists>
										<#list johnny[qq[0]] as col>
												<option value="${col[0].getidentifier()}">${col[1].gettitle()}</option>
										</#list>
								</#if>
						</select>
					</td>
					<td>
						<input type="submit" name="SearchSession" value="<@spring.message 'search_t' />" class="button" />
						<@authz.authorize ifAllGranted="ROLE_USER">
								<input type="submit" name="SaveSession" value="<@spring.message 'saveButton_t' />" class="button" />
						</@authz.authorize>
					</td>
				</tr>
        </form>
    </#list>

<#else>
		<tr>
			<td colspan="4"><@spring.message "historyNone_t" /></th>
		</tr> 
   
</#if>
		</table><br />
<@authz.authorize ifAllGranted="ROLE_USER">

		<form method="POST" style="margin:0;" action="">
<table class="formtable" cellpadding="4" cellspacing="1" width="100%">
<col width="20"/><col width="200"/><col width=""/><col width="200"/><col width="200"/>
<tr>
	<th colspan="5" class="th_red"><@spring.message 'history_t' />:<em> <@authz.authentication operation="username" /></em></th>
</tr>
<#if errors?exists>
<tr>
	<td class="important attention" colspan="5">
    Errors:<br />
    <#list errors as error>
        <#if error = 'maxItems'>
					<@spring.message "historySavedFailed_t" /><br />			
					<@spring.message "maxItems_t" /><br />
        <#else>
					<@spring.message "historySavedFailed_t" /><br />
            - ${error}<br />
        </#if>
    </#list>
		</td>
</tr>
</#if>
<#if success?exists>
<tr><td colspan="5" class="ok">
    ${success}
</td></tr>
</#if>
<#if johnnySaved?exists>
    <#assign nbHist=querSaved?size+1>
     
    <#list querSaved as qq>
        <form method="POST">
		<#assign nbHist=nbHist-1>
			<tr>
         <td>${nbHist}</td>
					<td><input type="text" value='${qq[0]}' /><input type="hidden" name="fullUrl" value='${qq[2]}' style="width:200px"/></td>
           <td>
							<select name="hist" style="width:400px">
									<option value="all">all selection</option>
									<#if johnnySaved[qq[0]]?exists>
											<#list johnnySaved[qq[0]] as col>
													<option value="${col[0].getidentifier()}" title="${col[1].gettitle()}">${col[1].gettitle()}</option>
											</#list>
									</#if>
							</select>
						</td>
						<td>Date saved: ${qq[1]}</td>
						<td>
            <input type="submit" name="SearchSaved" value="SEARCH" class="button"/>
            <input type="submit" name="DeleteSaved" value="DELETE" class="button"/>
						</td>
        </tr>
        </form>
    </#list>

<#else>
    <@spring.message "userHistoryNone_t" /> <@authz.authentication operation="username" />
</#if>
</table>
</form>
<br />
</@authz.authorize>


</div>
<#include "/footer.ftl">