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

package eu.europeana.definitions.beans;

import eu.europeana.definitions.annotations.Europeana;
import eu.europeana.definitions.annotations.Solr;

import static eu.europeana.definitions.annotations.FieldCategory.COPY_FIELD;
import static eu.europeana.definitions.annotations.FieldCategory.ESE_OPTIONAL;

/**
 * todo: note that this is a copy of eu.europeana.core.querymodel.beans.* with SOLR @Field annotation removed
 * 
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jan 7, 2010 9:17:26 AM
 */

public class AllFieldBean extends FullBean {

    @Europeana(category = COPY_FIELD, facet = false, facetPrefix = "loc", fullDoc = false)
    @Solr(fieldType = "string")
    String[] location;

    @Europeana(category = COPY_FIELD, facet = false, facetPrefix = "cont", fullDoc = false)
    @Solr(fieldType = "string")
    String[] contributor;

    @Europeana(category = COPY_FIELD, facet = false, facetPrefix = "ut", fullDoc = false)
    @Solr(fieldType = "string")
    String[] userTags;

    @Europeana(category = COPY_FIELD, facet = false, facetPrefix = "sub", fullDoc = false)
    @Solr(fieldType = "string")
    String[] SUBJECT;


    @Europeana(category = ESE_OPTIONAL, fullDoc = false)
    @Solr(prefix = "europeana", localName = "unstored", stored = false)
    String[] europeanaUnstored;

    // copy fields
    @Europeana(category = COPY_FIELD, fullDoc = false)
    @Solr()
    String[] text;

    @Europeana(category = COPY_FIELD, fullDoc = false)
    @Solr()
    String[] description;

    @Europeana(category = COPY_FIELD, fullDoc = false)
    @Solr()
    String[] date;

    @Europeana(category = COPY_FIELD, fullDoc = false)
    @Solr()
    String[] format;

    @Europeana(category = COPY_FIELD, fullDoc = false)
    @Solr()
    String[] publisher;

    @Europeana(category = COPY_FIELD, fullDoc = false)
    @Solr()
    String[] source;

    @Europeana(category = COPY_FIELD, fullDoc = false)
    @Solr()
    String[] rights;

    @Europeana(category = COPY_FIELD, fullDoc = false)
    @Solr()
    String[] identifier;

    @Europeana(category = COPY_FIELD, fullDoc = false)
    @Solr()
    String[] relation;

    @Europeana(category = COPY_FIELD, fullDoc = false)
    @Solr()
    String[] subject;

    // wh copy fields
    @Europeana(category = COPY_FIELD, fullDoc = false)
    @Solr()
    String[] who;

    @Europeana(category = COPY_FIELD, fullDoc = false)
    @Solr()
    String[] when;

    @Europeana(category = COPY_FIELD, fullDoc = false)
    @Solr()
    String[] what;

    @Europeana(category = COPY_FIELD, fullDoc = false)
    @Solr()
    String[] where;
}
