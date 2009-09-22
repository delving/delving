<#assign model = result>
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<rss version="2.0" xmlns:media="http://search.yahoo.com/mrss">
<channel>
<title>Europeana</title>
<link>http://www.europeana.eu/</link>
<#list model.briefDocWindow.docs as doc>
    <item>
        <title>${doc.title}</title>
        <link>${doc.thumbnail}</link>
        <guid>${doc.id}</guid>
        <media:thumbnail url="${doc.thumbnail}"/>
        <media:content url="${doc.thumbnail}" type="image/jpeg" />
    </item>
</#list>
</channel>
</rss>
