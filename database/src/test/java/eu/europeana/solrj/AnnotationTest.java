package eu.europeana.solrj;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.beans.Field;
import org.junit.Test;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class AnnotationTest {

    private Logger log = Logger.getLogger(getClass());

    @Test
    public void readAnnotations() throws Exception {
        for (java.lang.reflect.Field field : FullBean.class.getDeclaredFields()) {
            Solr solr = field.getAnnotation(Solr.class);
            if (solr != null) {
                if (solr.toCopyField().length > 0) {
                    for (String copyField : solr.toCopyField()) {
                        log.info(field + " copy to " + copyField);
                    }
                } else {
                    log.info(field + " no copy fields");
                }
            }
        }
    }

    public static class FullBean {

        // copy fields

        @Field
        @Europeana(copyField = true)
        @Solr()
        String[] title;

        @Field
        @Europeana(copyField = true)
        @Solr()
        String[] description;

        @Field
        @Europeana(copyField = true)
        @Solr()
        String[] date;

        @Field
        @Solr()
        @Europeana(copyField = true)
        String creator;

        @Field
        @Europeana(copyField = true)
        @Solr()
        String[] format;

        @Field
        @Europeana(copyField = true)
        @Solr()
        String[] publisher;

        @Field
        @Europeana(copyField = true)
        @Solr()
        String[] source;

        @Field
        @Europeana(copyField = true)
        @Solr()
        String[] rights;

        @Field
        @Europeana(copyField = true)
        @Solr()
        String[] identifier;

        @Field
        @Europeana(copyField = true)
        @Solr()
        String[] relation;

        // wh copy fields
        @Field
        @Europeana(copyField = true)
        @Solr()
        String[] who;

        @Field
        @Europeana(copyField = true)
        @Solr()
        String[] when;

        @Field
        @Europeana(copyField = true)
        @Solr()
        String[] what;

        @Field
        @Europeana(copyField = true)
        @Solr()
        String[] where;


        // Facet Fields
        @Field("LANGUAGE")
        @Europeana(copyField = true, facet = true, facetPrefix = "lang")
        @Solr()
        String[] language;

        @Field("YEAR")
        @Europeana(copyField = true, facet = true, facetPrefix = "yr", briefDoc = true)
        @Solr()
        String year;

        @Field("PROVIDER")
        @Europeana(copyField = true, facet = true, facetPrefix = "prov", briefDoc = true)
        @Solr()
        String provider;

        @Field("TYPE")
        @Europeana(copyField = true, facet = true, facetPrefix = "type", briefDoc = true)
        @Solr()
        String docType;

        @Field("COUNTRY")
        @Europeana(copyField = true, facet = true, facetPrefix = "coun", briefDoc = true)
        @Solr()
        String country;

        // disabled facet fields

        @Field("LOCATION")
        @Europeana(copyField = true, facet = false, facetPrefix = "loc")
        @Solr()
        String location;

        @Field("CONTRIBUTOR")
        @Europeana(copyField = true, facet = false, facetPrefix = "cont")
        @Solr()
        String contributor;

        @Field("USERTAGS")
        @Europeana(copyField = true, facet = false, facetPrefix = "ut")
        @Solr()
        String userTags;

        @Field("SUBJECT")
        @Europeana(copyField = true, facet = false, facetPrefix = "sub")
        @Solr()
        String subject;


        // Europeana namespace

        // required field
        @Europeana(briefDoc = true)
        @Solr(namespace = "europeana", name = "uri", multivalued = false, required = true)
        @Field("europeana_uri")
        String europeanaUri;

        @Europeana
        @Solr(namespace = "europeana", name = "collectionName", multivalued = false, required = true)
        @Field("europeana_collectionName")
        String collectionName;

        @Europeana
        @Solr(namespace = "europeana", name = "type", multivalued = false, toCopyField = {"TYPE"})
        @Field("europeana_type")
        String europeanaType;

        @Europeana()
        @Solr(namespace = "europeana", name = "userTag", toCopyField = {"text", "USERTAGS"})
        @Field("europeana_userTag")
        String europeanaUserTag;

        @Europeana()
        @Solr(namespace = "europeana", name = "language", toCopyField = {"text", "LANGUAGE"})
        @Field("europeana_language")
        String europeanaLanguage;



    }
}