package eu.europeana.beans;

import org.apache.solr.client.solrj.beans.Field;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jan 7, 2010 9:16:46 AM
 */
public class BriefBean extends RequiredBean {

    @Field("TYPE")
    @Europeana(copyField = true, facet = true, facetPrefix = "type", briefDoc = true)
    @Solr(fieldType = "string")
    String[] docType;

    @Field("LANGUAGE")
    @Europeana(copyField = true, facet = true, facetPrefix = "lang", briefDoc = true)
    @Solr(fieldType = "string")
    String[] language;

    @Field("YEAR")
    @Europeana(copyField = true, facet = true, facetPrefix = "yr", briefDoc = true)
    @Solr(fieldType = "string")
    String[] year;

    @Field
    @Europeana(copyField = true, briefDoc = true)
    @Solr()
    String[] title;

    @Field
    @Solr()
    @Europeana(copyField = true, briefDoc = true)
    String creator;

    @Europeana(briefDoc = true)
    @Solr(namespace = "europeana", name = "object")
    @Field("europeana_object")
    String[] europeanaObject;

}
