package eu.europeana.beans;

import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Interpret the annotations in the beans which define the search model
 *
 * @author Gerald de Jong geralddejong@gmail.com
 */

public class AnnotationProcessorImpl implements AnnotationProcessor {

    private Logger log = Logger.getLogger(getClass());
    private Set<FacetFieldImpl> facetFields = new HashSet<FacetFieldImpl>();
    private Map<Class<?>, Set<Field>> beanFieldMap = new HashMap<Class<?>, Set<Field>>();

    public void setClasses(List<Class<?>> classes) {
        for (Class<?> c : classes) {
            processAnnotations(c);
        }
    }

    public Set<? extends FacetField> getFacetFields() {
        return facetFields;
    }

    public Set<Field> getFields(Class<?> c) {
        return beanFieldMap.get(c);
    }

    // from here private

    private void processAnnotations(Class<?> c) {
        if (!beanFieldMap.containsKey(c)) {
            Set<Field> fieldSet = new HashSet<Field>();
            Class<?> clazz = c;
            while (clazz != Object.class) {
                for (Field field : clazz.getDeclaredFields()) {
                    processSolrAnnotation(field);
                    processEuropeanaAnnotation(field);
                    fieldSet.add(field);
                }
                clazz = clazz.getSuperclass();
            }
            beanFieldMap.put(c, fieldSet);
        }
    }

    private void processEuropeanaAnnotation(Field field) {
        Europeana europeana = field.getAnnotation(Europeana.class);
        if (europeana != null) {
            logEuropeanaAttributes(field, europeana);
            if (europeana.facet()) {
                facetFields.add(new FacetFieldImpl(field));
            }
        }
    }

    private void processSolrAnnotation(Field field) {
        Solr solr = field.getAnnotation(Solr.class);
        if (solr != null) {
            logSolrAttributes(field, solr);
        }
    }

    private void logEuropeanaAttributes(Field field, Europeana europeana) {
        String prefix = "Field ["+field.getName()+"]: ";
        log.info(prefix + "facetPrefix="+europeana.facetPrefix());
        log.info(prefix + "briefDoc="+europeana.briefDoc());
        log.info(prefix + "fullDoc="+europeana.fullDoc());
        log.info(prefix + "copyField(flag)="+europeana.copyField());
        log.info(prefix + "facet="+europeana.facet());
        log.info(prefix + "hidden="+europeana.hidden());
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

    private class FacetFieldImpl implements FacetField {
        private Field field;

        private FacetFieldImpl(Field field) {
            this.field = field;
        }

        @Override
        public String getPrefix() {
            return field.getAnnotation(Europeana.class).facetPrefix();
        }

        @Override
        public String getName() {
            String name = field.getAnnotation(org.apache.solr.client.solrj.beans.Field.class).value();
            if (!name.equals(org.apache.solr.client.solrj.beans.Field.DEFAULT)) {
                return name;
            }
            else {
                return field.getName();
            }
        }

        @Override
        public String getFieldNameString() {
            return getPrefix()+'_'+getName();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FacetFieldImpl that = (FacetFieldImpl) o;
            return !(field != null ? !field.equals(that.field) : that.field != null);
        }

        @Override
        public int hashCode() {
            return field != null ? field.hashCode() : 0;
        }
    }
}