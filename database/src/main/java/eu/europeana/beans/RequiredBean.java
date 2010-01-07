package eu.europeana.beans;

import org.apache.solr.client.solrj.beans.Field;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jan 7, 2010 9:16:57 AM
 */
public class RequiredBean extends IdBean {

    @Europeana
    @Solr(namespace = "europeana", name = "collectionName", multivalued = false, required = true)
    @Field("europeana_collectionName")
    String collectionName;

    @Field("PROVIDER")
    @Europeana(copyField = true, facet = true, facetPrefix = "prov", briefDoc = true)
    @Solr(fieldType = "string")
    String[] provider;

}
