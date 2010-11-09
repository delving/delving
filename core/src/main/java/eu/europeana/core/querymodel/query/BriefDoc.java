/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.0 or - as soon they
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

package eu.europeana.core.querymodel.query;

import eu.delving.core.binding.FieldValue;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public interface BriefDoc { // if multiple-> show first
    int getIndex();
    String getFullDocUrl();
    String getId();
    String getTitle();
    String getThumbnail();
    String getCreator();
    String getYear();
    String getProvider();
    String getDataProvider();
    String getLanguage(); // used to be Language
    DocType getType();
    // debug and scoring information
    int getScore();
    String getDebugQuery();

    // get FieldValue from bindingMap
    FieldValue getFieldValue(String key);

    void setIndex(int index);
    void setFullDocUrl(String fullDocUrl);
    void setScore(int score);
    void setDebugQuery(String debugQuery);
}