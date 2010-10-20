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

package eu.europeana.sip.definitions.beans;

import eu.europeana.sip.definitions.annotations.Europeana;
import eu.europeana.sip.definitions.annotations.Solr;
import eu.europeana.sip.definitions.presentation.BriefDoc;
import eu.europeana.sip.definitions.presentation.DocType;

import static eu.europeana.sip.definitions.annotations.FieldCategory.ESE_PLUS;
import static eu.europeana.sip.definitions.annotations.FieldCategory.INDEX_TIME_ADDITION;

/**
 * todo: note that this is a copy of eu.europeana.core.querymodel.beans.* with SOLR @Field annotation removed
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jan 7, 2010 9:16:46 AM
 */

public class BriefBean extends IdBean implements BriefDoc {

    transient int index;

    @Europeana(category = ESE_PLUS, constant = true)
    @Solr(prefix = "europeana", localName = "collectionName", multivalued = false, required = true)
    String[] europeanaCollectionName;

    @Europeana(category = ESE_PLUS, constant = true)
    @Solr(prefix = "europeana", localName = "collectionTitle", multivalued = false, required = true)
    String[] europeanaCollectionTitle;

    @Europeana(category = INDEX_TIME_ADDITION, facetPrefix = "prov", briefDoc = true)
    @Solr(fieldType = "string")
    String[] provider;

    @Europeana(briefDoc = true, object = true, url = true)
    @Solr(prefix = "europeana", localName = "object")
    String[] europeanaObject;

    @Europeana(category = INDEX_TIME_ADDITION, facetPrefix = "coun")
    @Solr(fieldType = "string")
    String[] country;

    @Europeana(category = INDEX_TIME_ADDITION, facetPrefix = "type", briefDoc = true, type = true)
    @Solr(localName = "type", fieldType = "string")
    String[] docType;

    @Europeana(category = INDEX_TIME_ADDITION, facetPrefix = "lang", briefDoc = true)
    @Solr(fieldType = "string")
    String[] language;

    @Europeana(category = INDEX_TIME_ADDITION, facetPrefix = "yr", briefDoc = true)
    @Solr(fieldType = "string")
    String[] year;

    @Europeana(category = INDEX_TIME_ADDITION, briefDoc = true)
    @Solr()
    String[] title;

    @Solr()
    @Europeana(category = INDEX_TIME_ADDITION, briefDoc = true)
    String[] creator;

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String getId() {
        return europeanaUri;
    }

    @Override
    public String getTitle() {
        return returnStringOrElse(title);
    }

    @Override
    public String getThumbnail() {
        return returnStringOrElse(europeanaObject);
    }

    @Override
    public String getCreator() {
        return returnStringOrElse(creator);
    }

    @Override
    public String getYear() {
        return returnStringOrElse(year);
    }

    @Override
    public String getProvider() {
        return returnStringOrElse(provider);
    }

    @Override
    public String getLanguage() {
        return returnStringOrElse(language);
    }

    @Override
    public DocType getType() {
        return DocType.get(docType);
    }

    static String returnStringOrElse(String[] s) {
        return (s != null) ? s[0] : "";
    }
}
