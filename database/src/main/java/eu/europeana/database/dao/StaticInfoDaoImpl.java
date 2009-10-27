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
import eu.europeana.database.domain.Partner;
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
}