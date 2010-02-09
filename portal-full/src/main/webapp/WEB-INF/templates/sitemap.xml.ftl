<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
<#compress>
    <#if idBeanList?? && fullViewUrl??>
        <#list idBeanList as idBean>
            <url>
                <loc>${fullViewUrl}?uri=${idBean.europeanaUri}</loc>
                <lastmod>${idBean.timestamp?string("yyyy-MM-dd")}</lastmod>
                <changefreq>monthly</changefreq>
            </url>
        </#list>
    </#if>
</#compress>
</urlset>
