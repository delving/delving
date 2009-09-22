<table id="tblContributors" class="tblList">
    <thead>
        <tr>
            <th><@spring.message 'Country_t' /></th>
            <th><@spring.message 'Contributor_t' /></th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td colspan="2">
                <p><@spring.message 'ContributorExplain_t' /></p>
                <p><@spring.message 'AggregatorExplain_t' /></p>
            </td>
        </tr>
        <#list contributors as contributor >
            <tr>
                <td>${contributor.country.englishName}</td>
                <td><#if contributor.url??><a href="${contributor.url}" target="_blank"/></#if>${contributor.originalName}
                    <#if contributor.numberOfPartners??>(${contributor.numberOfPartners})</#if></td>
            </tr>
        </#list>
    </tbody>
</table>