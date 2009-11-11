/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
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

package eu.europeana.database;

import eu.europeana.database.domain.Language;
import eu.europeana.database.domain.MessageKey;
import eu.europeana.database.domain.StaticPage;
import eu.europeana.database.domain.StaticPageType;
import eu.europeana.database.domain.Translation;

import java.util.List;
import java.util.Map;
import java.util.Set;

// todo: this entire dao will be eliminated

public interface MessageDao {

    // todo: remove these.. move implementations to LanguageDao
    /*
    Translation setTranslation(String key, Language language, String value);
    List<String> fetchMessageKeyStrings();
    MessageKey fetchMessageKey(String key);
    Map<String, List<Translation>> fetchTranslations(Set<String> languageCodes);

    //  moved implementations to StaticInfoDaoImpl
    StaticPage fetchStaticPage (Language language, String pageName);
    void setStaticPage(StaticPageType pageType, Language language, String content);
    List<StaticPage> getAllStaticPages();
    List<MessageKey> getAllTranslationMessages();
    */
}