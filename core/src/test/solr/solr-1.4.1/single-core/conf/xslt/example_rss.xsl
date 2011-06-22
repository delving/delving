<?xml version='1.0' encoding='UTF-8'?>

<!--
  ~ Copyright 2011 DELVING BV
  ~
  ~ Licensed under the EUPL, Version 1.1 or as soon they
  ~ will be approved by the European Commission - subsequent
  ~ versions of the EUPL (the "Licence");
  ~ you may not use this work except in compliance with the
  ~ Licence.
  ~ You may obtain a copy of the Licence at:
  ~
  ~ http://ec.europa.eu/idabc/eupl
  ~
  ~ Unless required by applicable law or agreed to in
  ~ writing, software distributed under the Licence is
  ~ distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  ~ express or implied.
  ~ See the Licence for the specific language governing
  ~ permissions and limitations under the Licence.
  -->

<!-- 
  Simple transform of Solr query results to RSS
 -->

<xsl:stylesheet version='1.0'
    xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>

  <xsl:output
       method="xml"
       encoding="utf-8"
       media-type="text/xml; charset=UTF-8"
  />
  <xsl:template match='/'>
    <rss version="2.0">
       <channel>
	 <title>Example Solr RSS 2.0 Feed</title>
         <link>http://localhost:8983/solr</link>
         <description>
          This has been formatted by the sample "example_rss.xsl" transform -
          use your own XSLT to get a nicer RSS feed.
         </description>
         <language>en-us</language>
         <docs>http://localhost:8983/solr</docs>
         <xsl:apply-templates select="response/result/doc"/>
       </channel>
    </rss>
  </xsl:template>
  
  <!-- search results xslt -->
  <xsl:template match="doc">
    <xsl:variable name="id" select="str[@name='id']"/>
    <xsl:variable name="timestamp" select="date[@name='timestamp']"/>
    <item>
      <title><xsl:value-of select="str[@name='name']"/></title>
      <link>
        http://localhost:8983/solr/select?q=id:<xsl:value-of select="$id"/>
      </link>
      <description>
        <xsl:value-of select="arr[@name='features']"/>
      </description>
      <pubDate><xsl:value-of select="$timestamp"/></pubDate>
      <guid>
        http://localhost:8983/solr/select?q=id:<xsl:value-of select="$id"/>
      </guid>
    </item>
  </xsl:template>
</xsl:stylesheet>
