/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.europeana.beans.annotation;

import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Interpret the annotations in the beans which define the search model, and
 * reveal the results through the AnnotationProcessor interface.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class AnnotationProcessorImpl implements AnnotationProcessor {
    private Logger log = Logger.getLogger(getClass());
    private Set<EuropeanaFieldImpl> facetFields = new HashSet<EuropeanaFieldImpl>();
    private Set<EuropeanaFieldImpl> solrFields = new HashSet<EuropeanaFieldImpl>();
    private String [] facetFieldStrings;
    private List<String> solrFieldList;
    private Map<Class<?>, EuropeanaBean> beanMap = new HashMap<Class<?>, EuropeanaBean>();

    /**
     * Configure the annotation processor to analyze the given list of classes
     *
     * @param classes a list of class objects
     */

    public void setClasses(List<Class<?>> classes) {
        for (Class<?> c : classes) {
            processAnnotations(c);
        }
    }

    @Override
    public Set<? extends EuropeanaField> getFacetFields() {
        return facetFields;
    }

    @Override
    public Set<? extends EuropeanaField> getSolrFields() {
        return solrFields;
    }

    @Override
    public List<String> getSolrFieldList() {
        if (solrFieldList == null) {
            solrFieldList = new ArrayList<String>();
            for (EuropeanaFieldImpl solrField : solrFields) {
                solrFieldList.add(solrField.getSolrFieldName());
            }
        }
        return solrFieldList;
    }

    @Override
    public String[] getFacetFieldStrings() {
        if (facetFieldStrings == null) {
            facetFieldStrings = new String[facetFields.size()];
            int index = 0;
            for (EuropeanaField facetField : facetFields) {
                facetFieldStrings[index] = facetField.getName();
                index++;
            }
        }
        return facetFieldStrings;
    }

    @Override
    public EuropeanaBean getEuropeanaBean(Class<?> c) {
        return beanMap.get(c);
    }

    // from here private

    private void processAnnotations(Class<?> c) {
        if (!beanMap.containsKey(c)) {
            log.info("Processing "+c.getCanonicalName());
            EuropeanaBeanImpl europeanaBean = new EuropeanaBeanImpl(c);
            Class<?> clazz = c;
            while (clazz != Object.class) {
                for (Field field : clazz.getDeclaredFields()) {
                    if (!Modifier.isTransient(field.getModifiers())) {
                        EuropeanaFieldImpl europeanaField = new EuropeanaFieldImpl(field);
                        if (europeanaField.isFacet()) {
                            facetFields.add(europeanaField);
                        }
                        solrFields.add(europeanaField);
                        europeanaBean.addField(europeanaField);
                    }
                }
                clazz = clazz.getSuperclass();
            }
            beanMap.put(c, europeanaBean);
        }
    }

//    private void logEuropeanaAttributes(Field field, Europeana europeana) {
//        String prefix = "Field ["+field.getName()+"]: ";
//        log.info(prefix + "facetPrefix="+europeana.facetPrefix());
//        log.info(prefix + "briefDoc="+europeana.briefDoc());
//        log.info(prefix + "fullDoc="+europeana.fullDoc());
//        log.info(prefix + "copyField(flag)="+europeana.copyField());
//        log.info(prefix + "facet="+europeana.facet());
//        log.info(prefix + "hidden="+europeana.hidden());
//    }
//
//    private void logSolrAttributes(Field field, Solr solr) {
//        String prefix = "Field ["+field.getName()+"]: ";
//        log.info(prefix + "name="+solr.name());
//        log.info(prefix + "namespace="+solr.namespace());
//        log.info(prefix + "fieldType="+solr.fieldType());
//        log.info(prefix + "indexed="+solr.indexed());
//        log.info(prefix + "multivalued="+solr.multivalued());
//        log.info(prefix + "defaultValue="+solr.defaultValue());
//        log.info(prefix + "omitNorms="+solr.omitNorms());
//        log.info(prefix + "required="+solr.required());
//        log.info(prefix + "stored="+solr.stored());
//        log.info(prefix + "termOffsets="+solr.termOffsets());
//        log.info(prefix + "termPositions="+solr.termPositions());
//        log.info(prefix + "termVectors="+solr.termVectors());
//        log.info(prefix + "compressed="+solr.compressed());
//        for (String copyField : solr.toCopyField()) {
//            log.info(prefix + "copyField="+copyField);
//        }
//    }

    private static class EuropeanaBeanImpl implements EuropeanaBean {
        private Class<?> beanClass;
        private Set<EuropeanaField> fields = new HashSet<EuropeanaField>();
        private String [] fieldStrings;

        private EuropeanaBeanImpl(Class<?> beanClass) {
            this.beanClass = beanClass;
            if (beanClass.getAnnotation(EuropeanaView.class) == null) {
                throw new RuntimeException("Bean class must be annotated with @EuropeanaView");
            }
        }

        @Override
        public int rows() {
            return beanClass.getAnnotation(EuropeanaView.class).rows();
        }

        @Override
        public boolean facets() {
            return beanClass.getAnnotation(EuropeanaView.class).facets();
        }

        @Override
        public Set<EuropeanaField> getFields() {
            return fields;
        }

        @Override
        public String[] getFieldStrings() {
            if (fieldStrings == null) {
                fieldStrings = new String[fields.size()];
                int index = 0;
                for (EuropeanaField europeanaField : fields) {
                    fieldStrings[index] = europeanaField.getName();
                    index++;
                }
            }
            return fieldStrings;
        }

        void addField(EuropeanaField field) {
            fields.add(field);
        }
    }

    private static class EuropeanaFieldImpl implements EuropeanaField {
        private Field field;
        private Europeana europeanaAnnotation;
        private org.apache.solr.client.solrj.beans.Field fieldAnnotation;
        private Solr solrAnnotation;
        private String fieldNameString;

        private EuropeanaFieldImpl(Field field) {
            this.field = field;
            europeanaAnnotation = field.getAnnotation(Europeana.class);
            fieldAnnotation = field.getAnnotation(org.apache.solr.client.solrj.beans.Field.class);
            solrAnnotation = field.getAnnotation(Solr.class);
            if (europeanaAnnotation == null) {
                throw new IllegalStateException("Field must have @Europeana annotation: "+field.getDeclaringClass().getName()+"."+field.getName());
            }
            if (fieldAnnotation == null) {
                throw new IllegalStateException("Field must have solrj @Field annotation: "+field.getDeclaringClass().getName()+"."+field.getName());
            }
            if (solrAnnotation == null) {
                throw new IllegalStateException("Field must have solrj @Solr annotation: "+field.getDeclaringClass().getName()+"."+field.getName());
            }
        }

        @Override
        public String getPrefix() {
            if (europeanaAnnotation.facet()) {
                return europeanaAnnotation.facetPrefix();
            }
            else {
                return solrAnnotation.namespace();
            }
        }

        @Override
        public String getName() {
            String name = fieldAnnotation.value();
            if (name.equals(org.apache.solr.client.solrj.beans.Field.DEFAULT)) {
                name = field.getName();
            }
            return name;
        }

        @Override
        public String getIndexName() {
            String name = solrAnnotation.name();
            if (name.isEmpty()) {
                name = fieldAnnotation.value();
                if (name.equals(org.apache.solr.client.solrj.beans.Field.DEFAULT)) {
                    name = field.getName();
                }
            }
            return name;
        }

        @Override
        public String getSolrFieldName() {
            String name = fieldAnnotation.value();
                if (name.equals(org.apache.solr.client.solrj.beans.Field.DEFAULT)) {
                    name = field.getName();
                }
            return name;
        }

        @Override
        public String getFieldNameString() {
            if (fieldNameString == null) {
                if (europeanaAnnotation.facet() || getPrefix().isEmpty()) {
                    fieldNameString = getName();
                }
                else {
                    fieldNameString = getPrefix()+'_'+getIndexName();
                }
            }
            return fieldNameString;
        }

        @Override
        public boolean isFacet() {
            return europeanaAnnotation.facet();
        }

        @Override
        public String getFacetName() {
            return fieldAnnotation.value();
        }

        @Override
        public boolean isEuropeanaUri() {
            return europeanaAnnotation.id();
        }

        @Override
        public boolean isEuropeanaObject() {
            return europeanaAnnotation.object();
        }

        @Override
        public boolean isEuropeanaType() {
            return europeanaAnnotation.type();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EuropeanaFieldImpl that = (EuropeanaFieldImpl) o;
            return !(field != null ? !field.equals(that.field) : that.field != null);
        }

        @Override
        public int hashCode() {
            return field != null ? field.hashCode() : 0;
        }

        public String toString() {
            return field.getName();
        }
    }
}