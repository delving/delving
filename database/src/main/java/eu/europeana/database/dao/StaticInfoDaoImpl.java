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

import eu.europeana.database.StaticInfoDao;
import eu.europeana.database.domain.Contributor;
import eu.europeana.database.domain.Language;
import eu.europeana.database.domain.MessageKey;
import eu.europeana.database.domain.Partner;
import eu.europeana.database.domain.StaticPage;
import eu.europeana.database.domain.StaticPageType;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */
public class StaticInfoDaoImpl implements StaticInfoDao {

    @PersistenceContext
    protected EntityManager entityManager;

    @Transactional
    @SuppressWarnings("unchecked")
    public List<Partner> getAllPartnerItems() {
        Query q = entityManager.createQuery("select pi from Partner pi order by pi.sector");
        return (List<Partner>) q.getResultList();
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public List<Contributor> getAllContributorItems() {
        Query q = entityManager.createQuery("select con from Contributor con order by con.country");
        return (List<Contributor>) q.getResultList();
    }

    @Transactional
    public void saveContributor(Contributor contributorX) {
        Query query = entityManager.createQuery("select co from Contributor as co where co.providerId = :providerId");
        query.setParameter("providerId", contributorX.getProviderId());
        Contributor contributor = null;
        try {
            contributor = (Contributor) query.getSingleResult();
            contributor.setProviderId(contributorX.getProviderId());
            contributor.setOriginalName(contributorX.getOriginalName());
            contributor.setEnglishName(contributorX.getEnglishName());
            contributor.setAcronym(contributorX.getAcronym());
            contributor.setCountry(contributorX.getCountry());
            contributor.setNumberOfPartners(contributorX.getNumberOfPartners());
            contributor.setUrl(contributorX.getUrl());
        } catch (NoResultException e) {
            if (contributorX.getProviderId() != null) {
                entityManager.persist(contributorX);
            }
        }
    }
    

    @Transactional
    public void savePartner(Partner partnerX) {
        Query query = entityManager.createQuery("select po from Partner as po where po.name = :name");
        query.setParameter("name", partnerX.getName());
        Partner partner = null;
        try {
            partner = (Partner) query.getSingleResult();
            partner.setName(partnerX.getName());
            partner.setUrl(partnerX.getUrl());
            partnerX.setSector(partnerX.getSector());
        } catch (Exception e) {
            if (partnerX.getName() != null) {
                entityManager.persist(partnerX);
            }
        }
    }
    @SuppressWarnings("unchecked")
    @Transactional
    public List<StaticPage> getAllStaticPages() {
        Query query = entityManager.createQuery("select sp from StaticPage as sp");
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public List<MessageKey> getAllTranslationMessages() {
        Query query = entityManager.createQuery("select trans from Translation as trans");
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
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