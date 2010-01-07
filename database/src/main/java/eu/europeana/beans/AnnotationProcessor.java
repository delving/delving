package eu.europeana.beans;

import org.apache.log4j.Logger;

import java.lang.reflect.Field;

/**
 * Interpret the annotations in the beans which define the search model
 *
 * @author Gerald de Jong geralddejong@gmail.com
 */

public class AnnotationProcessor {

    private Logger log = Logger.getLogger(getClass());

    public AnnotationProcessor(Class<?>... classes) {
        for (Class<?> c : classes) {
            processAnnotations(c);
        }
    }

    private void processAnnotations(Class<?> c) {
        for (Field field : c.getDeclaredFields()) {
            processSolrAnnotation(field);
            processEuropeanaAnnotation(field);
        }
    }

    private void processEuropeanaAnnotation(Field field) {
        Europeana europeana = field.getAnnotation(Europeana.class);
        if (europeana != null) {
            logEuropeanaAttributes(field, europeana);
        }
    }

    private void logEuropeanaAttributes(Field field, Europeana europeana) {
        String prefix = "Field ["+field.getName()+"]: ";
        log.info(prefix + "facetPrefix="+europeana.facetPrefix());
        log.info(prefix + "facetName="+europeana.facetName());
        log.info(prefix + "briefDoc="+europeana.briefDoc());
        log.info(prefix + "fullDoc="+europeana.fullDoc());
        log.info(prefix + "copyField(flag)="+europeana.copyField());
        log.info(prefix + "facet="+europeana.facet());
        log.info(prefix + "hidden="+europeana.hidden());
    }

    private void processSolrAnnotation(Field field) {
        Solr solr = field.getAnnotation(Solr.class);
        if (solr != null) {
            logSolrAttributes(field, solr);
        }
    }

    private void logSolrAttributes(Field field, Solr solr) {
        String prefix = "Field ["+field.getName()+"]: ";
        log.info(prefix + "name="+solr.name());
        log.info(prefix + "namespace="+solr.namespace());
        log.info(prefix + "fieldType="+solr.fieldType());
        log.info(prefix + "indexed="+solr.indexed());
        log.info(prefix + "multivalued="+solr.multivalued());
        log.info(prefix + "defaultValue="+solr.defaultValue());
        log.info(prefix + "omitNorms="+solr.omitNorms());
        log.info(prefix + "required="+solr.required());
        log.info(prefix + "stored="+solr.stored());
        log.info(prefix + "termOffsets="+solr.termOffsets());
        log.info(prefix + "termPositions="+solr.termPositions());
        log.info(prefix + "termVectors="+solr.termVectors());
        log.info(prefix + "compressed="+solr.compressed());
        for (String copyField : solr.toCopyField()) {
            log.info(prefix + "copyField="+copyField);
        }
    }


}
