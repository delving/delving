<table id="tblPartners" class="tblList">
    <thead>
        <tr>
            <th><@spring.message 'Sector_t' /></th>
            <th><@spring.message 'Partner_t' /></th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td colspan="2">
                <p>
                <@spring.message 'PartnerExplain_t' />
                </p>
            </td>
        </tr>
        <#list partners as partner >
            <tr>
                <td>${partner.sector.viewName}</td>
                <td><#if partner.url??><a href="${partner.url}" target="_blank"/></#if>${partner.name}</td>
            </tr>
        </#list>
    </tbody>
</table>