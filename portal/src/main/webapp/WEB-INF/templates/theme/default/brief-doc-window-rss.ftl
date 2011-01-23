<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<rss version="2.0" xmlns:media="http://search.yahoo.com/mrss" xmlns:atom="http://www.w3.org/2005/Atom">
<channel>
<title>Delving</title>
<link>http://www.delving.eu/</link>
    <atom:link rel="self" href="${servletUrl}/search?query=${query}&amp;start=${pagination.start}&amp;format=rss" type="application/rss+xml" />
    <#if pagination.isPrevious()>
    <atom:link rel="previous" href="${servletUrl}/search?query=${query}&amp;start=${pagination.previousPage}&amp;format=rss" type="application/rss+xml" />
    </#if>
    <#if pagination.isNext()>
    <atom:link rel="next" href="${servletUrl}/search?query=${query}&amp;start=${pagination.nextPage}&amp;format=rss" type="application/rss+xml" />
    </#if>
    <#list model.briefDocWindow.docs as doc>
    <item>
        <title>${doc.title}</title>
        <link>${doc.id}</link>
        <guid>${doc.id}</guid>
        <media:thumbnail url="${doc.thumbnail?url('utf-8')}"/>
        <media:content url="${doc.thumbnail?url('utf-8')}" type="image/jpeg" />
    </item>
    </#list>
</channel>
</rss>

