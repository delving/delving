package eu.europeana.beans;

import org.apache.solr.client.solrj.beans.Field;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jan 7, 2010 9:16:57 AM
 */
public class RequiredBean extends IdBean {

    @Europeana
    @Solr(namespace = "europeana", name = "europeanaCollectionName", multivalued = false, required = true)
    @Field("europeana_collectionName")
    String europeanaCollectionName;

    @Field("PROVIDER")
    @Europeana(copyField = true, facet = true, facetPrefix = "prov", briefDoc = true)
    @Solr(fieldType = "string")
    String[] provider;

    // todo determine if this really belongs here
    @Europeana(briefDoc = true)
    @Solr(namespace = "europeana", name = "object")
    @Field("europeana_object")
    String[] europeanaObject;
}
