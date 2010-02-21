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

package eu.europeana.core.querymodel.annotation;

/**
 * Reveal information from the annotated bean field
 *
 * @author Gerald de Jong geralddejong@gmail.com
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public interface EuropeanaField {

    /**
     * How does its name begin?
     * @return the first part
     */

    String getPrefix();

    /**
     * How does the name end?
     * @return the second part
     */

    String getName();

    /**
     * A combination of prefix and name, separated by underscore
     * @return the name of this field according to Solr
     */

    String getFieldNameString();

    /**
     * Reveal whether this is a facet field
     * @return true if it is
     */

    boolean isFacet();

    /**
     * When this is a facet field, reveal its name
     * @return the name of the facet
     */

    String getFacetName();

    /**
     * Show whether this field is the europeana URI
     * @return true if this field is the one
     */

    String getFacetPrefix();


    boolean isEuropeanaUri();

    boolean isEuropeanaObject();

    boolean isEuropeanaType();

    String getIndexName();

    String getSolrFieldName();
}
