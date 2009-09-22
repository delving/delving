<#assign result = result/>
<#assign model = result/>
<#assign query = query/>
<#assign pagination = pagination/>
<#assign cacheUrl = cacheUrl/>
<#assign servletUrl = servletUrl/>
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<rss version="2.0" xmlns:media="http://search.yahoo.com/mrss" xmlns:atom="http://www.w3.org/2005/Atom">
<channel>
<title>Europeana</title>
<link>http://www.europeana.eu/</link>
    <atom:link rel="self" href="${servletUrl}/brief-doc.rss?query=${query}&amp;start=${pagination.start}&amp;format=rss" type="application/rss+xml" />
    <#if pagination.isPrevious()>
    <atom:link rel="previous" href="${servletUrl}/brief-doc.rss?query=${query}&amp;start=${pagination.previousPage}&amp;format=rss" type="application/rss+xml" />
    </#if>
    <#if pagination.isNext()>
    <atom:link rel="next" href="${servletUrl}/brief-doc.rss?query=${query}&amp;start=${pagination.nextPage}&amp;format=rss" type="application/rss+xml" />
    </#if>
    <#list model.briefDocWindow.docs as doc>
    <item>
        <title>${doc.title?html}</title>
        <!--suppress HtmlExtraClosingTag -->
        <link>${doc.id}</link>
        <guid>${doc.id}</guid>
        <media:thumbnail url="${doc.thumbnail?url('utf-8')}"/>
        <media:content url="${doc.thumbnail?url('utf-8')}" type="image/jpeg" />
    </item>
    </#list>
</channel>
</rss>

