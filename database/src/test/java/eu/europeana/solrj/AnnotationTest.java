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
        for (java.lang.reflect.Field field : BriefBean.class.getDeclaredFields()) {
            Europeana europeana = field.getAnnotation(Europeana.class);
            if (europeana != null) {
                if (europeana.copyFields().length > 0) {
                    for (String copyField : europeana.copyFields()) {
                        log.info(field + " copy to " + copyField);
                    }
                } else {
                    log.info(field + " no copy fields");
                }
            }
        }
    }

    public static class BriefBean {

        @Field
        String id;

        @Field
        @Europeana
        String[] title;

        @Field
        @Europeana(copyFields = {"dcterms_alternative", "gumby"})
        String creator;

        @Field("YEAR")
        String year;

        @Field("PROVIDER")
        String provider;

        @Field("europeana_collectionName")
        String collectionName;

        @Field("europeana_uri")
        String europeanaUri;
    }
}