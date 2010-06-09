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
import eu.europeana.definitions.annotations.FieldCategory;
import eu.europeana.definitions.annotations.Solr;
import eu.europeana.definitions.presentation.DocId;

import java.util.Date;

/**
 * todo: note that this is a copy of eu.europeana.core.querymodel.beans.* with SOLR @Field annotation removed
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jan 7, 2010 9:15:43 AM
 */

public class IdBean implements DocId {

    @Europeana(briefDoc = true, id = true, category = FieldCategory.ESE_PLUS, required = true, converter = "createEuropeanaURI", url = true)
    @Solr(prefix = "europeana", localName = "uri", multivalued = false, required = true)
    String europeanaUri;

    @Europeana(category = FieldCategory.INDEX_TIME_ADDITION)
    @Solr(localName = "timestamp", multivalued = false, defaultValue = "NOW")
    Date timestamp;

    @Override
    public String getEuropeanaUri() {
        return europeanaUri;
    }

    @Override
    public Date getTimestamp() {
        return timestamp;
    }
}
