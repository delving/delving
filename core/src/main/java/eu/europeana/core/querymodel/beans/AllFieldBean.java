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

package eu.europeana.core.querymodel.beans;

import eu.europeana.definitions.annotations.Europeana;
import eu.europeana.definitions.annotations.Solr;
import org.apache.solr.client.solrj.beans.Field;

import static eu.europeana.definitions.annotations.FieldCategory.ESE;
import static eu.europeana.definitions.annotations.FieldCategory.INDEX_TIME_ADDITION;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jan 7, 2010 9:17:26 AM
 */

public class AllFieldBean extends FullBean {

    @Field("LOCATION")
    @Europeana(category = INDEX_TIME_ADDITION, fullDoc = false)
    @Solr(fieldType = "string")
    String[] location;

    @Field("CONTRIBUTOR")
    @Europeana(category = INDEX_TIME_ADDITION, fullDoc = false)
    @Solr(fieldType = "string")
    String[] contributor;

    @Field("USERTAGS")
    @Europeana(category = INDEX_TIME_ADDITION, fullDoc = false)
    @Solr(fieldType = "string")
    String[] userTags;

    @Field("SUBJECT")
    @Europeana(category = INDEX_TIME_ADDITION, fullDoc = false)
    @Solr(fieldType = "string")
    String[] SUBJECT;


    @Europeana(category = ESE, fullDoc = false)
    @Solr(prefix = "europeana", localName = "unstored", stored = false)
    @Field("europeana_unstored")
    String[] europeanaUnstored;

    // copy fields
    @Field
    @Europeana(category = INDEX_TIME_ADDITION, fullDoc = false)
    @Solr()
    String[] text;

    @Field
    @Europeana(category = INDEX_TIME_ADDITION, fullDoc = false)
    @Solr()
    String[] description;

    @Field
    @Europeana(category = INDEX_TIME_ADDITION, fullDoc = false)
    @Solr()
    String[] date;

    @Field
    @Europeana(category = INDEX_TIME_ADDITION, fullDoc = false)
    @Solr()
    String[] format;

    @Field
    @Europeana(category = INDEX_TIME_ADDITION, fullDoc = false)
    @Solr()
    String[] publisher;

    @Field
    @Europeana(category = INDEX_TIME_ADDITION, fullDoc = false)
    @Solr()
    String[] source;

    @Field
    @Europeana(category = INDEX_TIME_ADDITION, fullDoc = false)
    @Solr()
    String[] rights;

    @Field
    @Europeana(category = INDEX_TIME_ADDITION, fullDoc = false)
    @Solr()
    String[] identifier;

    @Field
    @Europeana(category = INDEX_TIME_ADDITION, fullDoc = false)
    @Solr()
    String[] relation;

    @Field
    @Europeana(category = INDEX_TIME_ADDITION, fullDoc = false)
    @Solr()
    String[] subject;

    // wh copy fields
    @Field
    @Europeana(category = INDEX_TIME_ADDITION, fullDoc = false)
    @Solr()
    String[] who;

    @Field
    @Europeana(category = INDEX_TIME_ADDITION, fullDoc = false)
    @Solr()
    String[] when;

    @Field
    @Europeana(category = INDEX_TIME_ADDITION, fullDoc = false)
    @Solr()
    String[] what;

    @Field
    @Europeana(category = INDEX_TIME_ADDITION, fullDoc = false)
    @Solr()
    String[] where;
}
