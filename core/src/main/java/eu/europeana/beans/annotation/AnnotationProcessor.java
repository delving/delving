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

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * This is the interface that defines what an AnnotationProcessor can reveal about
 * the bean being analyzed.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public interface AnnotationProcessor {

    /**
     * Retrieve instances of field specifications from the annotations
     *
     * @return a set of fields that play the facet role
     */

    Set<? extends EuropeanaField> getFacetFields();

    /**
     * Get an array of facet field names which can be passed easily
     * to the SolrQuery.
     *
     * @return an array of name strings
     */

    String [] getFacetFieldStrings();

    /**
     * Retrieve instances of field specifications from the annotations
     *
     * @return a set of all fields defined in bean classes
     */

    Set<? extends EuropeanaField> getSolrFields();

    /**
     * Get an array of all solr field names which can be passed easily
     * to the QueryAnalyser to validate the fielded query strings.
     *
     * @return an array of Solr name strings
     */

    List<String> getSolrFieldList();

    /**
     * Fetch the bean interface for the given bean class
     *
     * @param clazz the annotated bean class
     * @return the associated instance revealing annotation info
     */

    EuropeanaBean getEuropeanaBean(Class<?> clazz);

    HashMap<String, String> getFacetMap();
}
