package eu.europeana.beans;

import org.apache.solr.client.solrj.beans.Field;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jan 7, 2010 9:17:26 AM
 */

@EuropeanaView(facets = false, rows = 10)
public class AllFieldBean extends FullBean {

    // Facet Fields

    // disabled facet fields

    @Field("LOCATION")
    @Europeana(copyField = true, facet = false, facetPrefix = "loc", fullDoc = false)
    @Solr(fieldType = "string")
    String[] location;

    @Field("CONTRIBUTOR")
    @Europeana(copyField = true, facet = false, facetPrefix = "cont", fullDoc = false)
    @Solr(fieldType = "string")
    String[] contributor;

    @Field("USERTAGS")
    @Europeana(copyField = true, facet = false, facetPrefix = "ut", fullDoc = false)
    @Solr(fieldType = "string")
    String[] userTags;

    @Field("SUBJECT")
    @Europeana(copyField = true, facet = false, facetPrefix = "sub", fullDoc = false)
    @Solr(fieldType = "string")
    String[] subject;


    @Europeana(fullDoc = false)
    @Solr(namespace = "europeana", name = "unstored", stored = false)
    @Field("europeana_unstored")
    String[] europeanaUnstored;

    // copy fields
    @Field
    @Europeana(copyField = true, fullDoc = false)
    @Solr()
    String[] description;

    @Field
    @Europeana(copyField = true, fullDoc = false)
    @Solr()
    String[] date;

    @Field
    @Europeana(copyField = true, fullDoc = false)
    @Solr()
    String[] format;

    @Field
    @Europeana(copyField = true, fullDoc = false)
    @Solr()
    String[] publisher;

    @Field
    @Europeana(copyField = true, fullDoc = false)
    @Solr()
    String[] source;

    @Field
    @Europeana(copyField = true, fullDoc = false)
    @Solr()
    String[] rights;

    @Field
    @Europeana(copyField = true, fullDoc = false)
    @Solr()
    String[] identifier;

    @Field
    @Europeana(copyField = true, fullDoc = false)
    @Solr()
    String[] relation;

    // wh copy fields
    @Field
    @Europeana(copyField = true, fullDoc = false)
    @Solr()
    String[] who;

    @Field
    @Europeana(copyField = true, fullDoc = false)
    @Solr()
    String[] when;

    @Field
    @Europeana(copyField = true, fullDoc = false)
    @Solr()
    String[] what;

    @Field
    @Europeana(copyField = true, fullDoc = false)
    @Solr()
    String[] where;
}
