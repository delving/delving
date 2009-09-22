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

package eu.europeana.database.dao;

import eu.europeana.database.MessageDao;
import eu.europeana.database.domain.*;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;

@SuppressWarnings("unchecked")
public class MessageDaoImpl implements MessageDao {

    @PersistenceContext
    protected EntityManager entityManager;

    @Transactional
    public Translation setTranslation(String key, Language language, String value) {
        Query query = entityManager.createQuery("select mk from MessageKey mk where mk.key = :key");
        query.setParameter("key", key);
        List<MessageKey> messageKeys = (List<MessageKey>) query.getResultList();
        MessageKey messageKey;
        Translation translation;
        if (messageKeys.isEmpty()) {
            messageKey = new MessageKey(key);
            translation = messageKey.setTranslation(language, value);
            entityManager.persist(messageKey);
        }
        else {
            messageKey = messageKeys.get(0);
            translation = messageKey.setTranslation(language, value);
        }
        return translation;
    }

    @Transactional
    public List<String> fetchMessageKeyStrings() {
        Query query = entityManager.createQuery("select mk.key from MessageKey mk");
        return (List<String>) query.getResultList();
    }

    @Transactional
    public Map<String, List<Translation>> fetchTranslations(Set<String> languageCodes) {
        Map<String, List<Translation>> translations = new TreeMap<String, List<Translation>>();
        for (String languageCode : languageCodes) {
            Query query = entityManager.createQuery("select t from Translation t where t.language = :language");
            Language language = Language.findByCode(languageCode);
            query.setParameter("language", language);
            List<Translation> tlist = (List<Translation>)query.getResultList();
            for (Translation trans : tlist) {
                List<Translation> value = translations.get(trans.getMessageKey().getKey());
                if (value == null) {
                    value = new ArrayList<Translation>();
                    translations.put(trans.getMessageKey().getKey(), value);
                }
                value.add(trans);
            }
        }
        return translations;
    }

    @Transactional
    public List<StaticPage> getAllStaticPages() {
        Query query = entityManager.createQuery("select sp from StaticPage as sp");
        return query.getResultList();
    }

    @Transactional
    public List<MessageKey> getAllTranslationMessages() {
        Query query = entityManager.createQuery("select trans from Translation as trans");
        return query.getResultList();
    }

    @Transactional
    public MessageKey fetchMessageKey(String key) {
        Query query = entityManager.createQuery("select mk from MessageKey mk where mk.key = :key");
        query.setParameter("key", key);
        MessageKey messageKey = (MessageKey) query.getSingleResult();
        messageKey.getTranslations().size();
        return messageKey;
    }

    @Transactional
    public StaticPage fetchStaticPage (Language language, String pageName) {
        Query query = entityManager.createQuery("select sp from StaticPage as sp where sp.language = :language and sp.pageType = :pageType");
        query.setParameter("language", language);
        query.setParameter("pageType", StaticPageType.get(pageName));
        List results = query.getResultList();
        if (results.size() == 0) {
            query.setParameter("language", Language.EN);
            results = query.getResultList();
            if (results.size() == 0) {
                return new StaticPage(StaticPageType.get(pageName), language);
            }
        }
        return (StaticPage) results.get(0);
    }

    @Transactional
    public void setStaticPage(StaticPageType pageType, Language language, String content) {
        Query query = entityManager.createQuery("select sp from StaticPage sp where sp.pageType = :pageType and sp.language = :language");
        query.setParameter("pageType", pageType);
        query.setParameter("language", language);
        try {
            StaticPage page = (StaticPage)query.getSingleResult();
            page.setContent(content);
        }
        catch (NoResultException e) {
            StaticPage page = new StaticPage(pageType, language);
            page.setContent(content);
            entityManager.persist(page);
        }
    }

}