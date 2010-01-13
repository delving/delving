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

package eu.europeana.beans;

import eu.europeana.beans.annotation.Europeana;
import eu.europeana.beans.annotation.EuropeanaView;
import eu.europeana.beans.annotation.Solr;
import org.apache.solr.client.solrj.beans.Field;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jan 7, 2010 9:16:57 AM
 */

@EuropeanaView(facets = false, rows = 10)
public class RequiredBean extends IdBean {

    @Europeana
    @Solr(namespace = "europeana", name = "europeanaCollectionName", multivalued = false, required = true)
    @Field("europeana_collectionName")
    String europeanaCollectionName;

    @Field("PROVIDER")
    @Europeana(copyField = true, facet = true, facetPrefix = "prov", briefDoc = true)
    @Solr(fieldType = "string")
    String[] provider;

    // todo determine if this really belongs here
    @Europeana(briefDoc = true)
    @Solr(namespace = "europeana", name = "object")
    @Field("europeana_object")
    String[] europeanaObject;

    @Field("COUNTRY")
    @Europeana(copyField = true, facet = true, facetPrefix = "coun")
    @Solr(fieldType = "string")
    String[] country;
}
