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

package eu.europeana.definitions.annotations;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interpret the annotations in the beans which define the search model, and
 * reveal the results through the AnnotationProcessor interface.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class AnnotationProcessorImpl implements AnnotationProcessor {
    private Set<EuropeanaField> allFields = new HashSet<EuropeanaField>();
    private String[] facetFieldStrings;
    private List<String> solrFieldList;
    private Map<Class<?>, EuropeanaBean> beanMap = new HashMap<Class<?>, EuropeanaBean>();
    private HashMap<String, String> facetMap = new HashMap<String, String>();

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
    public Set<? extends EuropeanaField> getAllFields() {
        return allFields;
    }

    @Override
    public Set<? extends EuropeanaField> getMappableFields() {
        Set<EuropeanaField> mappableFields = new HashSet<EuropeanaField>();
        for (EuropeanaField field : allFields) {
            if (field.europeana().category() != FieldCategory.INDEX_TIME_ADDITION) {
                mappableFields.add(field);
            }
        }
        return mappableFields;
    }

    @Override
    public Set<? extends EuropeanaField> getFields(FieldCategory fieldCategory) {
        Set<EuropeanaField> fields = new HashSet<EuropeanaField>();
        for (EuropeanaField field : allFields) {
            if (field.europeana().category() == fieldCategory) {
                fields.add(field);
            }
        }
        return fields;
    }

    @Override
    public List<String> getFieldNameList() {
        if (solrFieldList == null) {
            solrFieldList = new ArrayList<String>();
            for (EuropeanaField solrField : allFields) {
                solrFieldList.add(solrField.getFieldNameString());
            }
        }
        return solrFieldList;
    }

    @Override
    public String[] getFacetFieldStrings() {
        if (facetFieldStrings == null) {
            List<String> facetFields = new ArrayList<String>();
            for (EuropeanaField field : allFields) {
                if (!field.europeana().facetPrefix().isEmpty()) {
                    facetFields.add(String.format("{!ex=%s}%s", field.europeana().facetPrefix(), field.getFacetName()));
                }
            }
            facetFieldStrings = facetFields.toArray(new String[facetFields.size()]);
        }
        return facetFieldStrings;
    }

    @Override
    public HashMap<String, String> getFacetMap() {
        return facetMap;
    }

    @Override
    public EuropeanaBean getEuropeanaBean(Class<?> c) {
        return beanMap.get(c);
    }

    // from here private

    private void processAnnotations(Class<?> c) {
        if (!beanMap.containsKey(c)) {
            EuropeanaBean europeanaBean = new EuropeanaBean();
            Class<?> clazz = c;
            while (clazz != Object.class) {
                for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                    if (!Modifier.isTransient(field.getModifiers())) {
                        EuropeanaField europeanaField = new EuropeanaField(field);
                        if (!europeanaField.europeana().facetPrefix().isEmpty()) {
                            facetMap.put(europeanaField.getFacetName(), europeanaField.europeana().facetPrefix());
                        }
                        allFields.add(europeanaField);
                        europeanaBean.addField(europeanaField);
                    }
                }
                clazz = clazz.getSuperclass();
            }
            beanMap.put(c, europeanaBean);
        }
    }

}
