<?xml version="1.0" encoding="UTF-8"?>
<sitemapindex xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
<#compress>
    <#if entries??>
        <#list entries as entry>
            <sitemap>
                <loc>${entry.loc}</loc>
                <#if entry.lastmod??><lastmod>${entry.lastmod?string("yyyy-MM-dd")}</lastmod></#if> 
            </sitemap>
        </#list>
    </#if>
</#compress>
</sitemapindex>
