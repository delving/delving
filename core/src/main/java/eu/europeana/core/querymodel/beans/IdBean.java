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

import eu.europeana.core.querymodel.query.DocId;
import eu.europeana.definitions.annotations.Europeana;
import eu.europeana.definitions.annotations.Solr;
import eu.europeana.definitions.annotations.ValidationLevel;
import org.apache.solr.client.solrj.beans.Field;

import java.util.Date;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jan 7, 2010 9:15:43 AM
 */

public class IdBean implements DocId {

    @Europeana(briefDoc = true, id = true, validation = ValidationLevel.ESE_PLUS_REQUIRED)
    @Solr(prefix = "europeana", localName = "uri", multivalued = false, required = true)
    @Field("europeana_uri")
    String europeanaUri;

    @Europeana(validation = ValidationLevel.INDEX_TIME_FIELD)
    @Solr(localName = "timestamp", multivalued = false, defaultValue = "NOW")
    @Field("timestamp")
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
