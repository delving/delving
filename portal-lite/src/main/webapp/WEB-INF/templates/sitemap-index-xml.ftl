<#assign entries = entries/>
<?xml version="1.0" encoding="UTF-8"?>
<sitemapindex xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
  <#list entries as entry>
  <sitemap>
    <loc>${entry.loc}</loc>
    <lastmod>${entry.lastmod}</lastmod>
  </sitemap>
  </#list>
</sitemapindex>
