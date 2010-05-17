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
import eu.europeana.definitions.annotations.ValidationLevel;
import eu.europeana.definitions.presentation.BriefDoc;
import eu.europeana.definitions.presentation.DocType;

/**
 * todo: note that this is a copy of eu.europeana.core.querymodel.beans.* with SOLR @Field annotation removed
 * 
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jan 7, 2010 9:16:46 AM
 */

public class BriefBean extends IdBean implements BriefDoc {

    transient int index;

    @Europeana(validation = ValidationLevel.ESE_PLUS_REQUIRED)
    @Solr(prefix = "europeana", localName = "europeanaCollectionName", multivalued = false, required = true)
    String[] europeanaCollectionName;

    @Europeana(validation = ValidationLevel.COPY_FIELD, facet = true, facetPrefix = "prov", briefDoc = true)
    @Solr(fieldType = "string")
    String[] provider;

    @Europeana(validation = ValidationLevel.ESE_REQUIRED, briefDoc = true, object = true, mappable = true)
    @Solr(prefix = "europeana", localName = "object")
    String[] europeanaObject;

    @Europeana(validation = ValidationLevel.COPY_FIELD, facet = true, facetPrefix = "coun")
    @Solr(fieldType = "string")
    String[] country;

    @Europeana(validation = ValidationLevel.COPY_FIELD, facet = true, facetPrefix = "type", briefDoc = true, type = true)
    @Solr(localName = "type", fieldType = "string")
    String[] docType;

    @Europeana(validation = ValidationLevel.COPY_FIELD, facet = true, facetPrefix = "lang", briefDoc = true)
    @Solr(fieldType = "string")
    String[] language;

    @Europeana(validation = ValidationLevel.COPY_FIELD, facet = true, facetPrefix = "yr", briefDoc = true, converter="extractYear")
    @Solr(fieldType = "string")
    String[] year;

    @Europeana(validation = ValidationLevel.COPY_FIELD, briefDoc = true)
    @Solr()
    String[] title;

    @Solr()
    @Europeana(validation = ValidationLevel.COPY_FIELD, briefDoc = true)
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
