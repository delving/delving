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

package eu.europeana.controller.util;

import eu.europeana.database.StaticInfoDao;
import eu.europeana.database.domain.Contributor;
import eu.europeana.database.domain.Partner;

import java.util.ArrayList;
import java.util.List;

/**
 * Refresh all the items from the Partner and Provider tables
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class PartnerListSampler {
    private StaticInfoDao staticInfoDao;
    private List<Partner> partnerCache = new ArrayList<Partner>();
    private List<Contributor> contributorCache = new ArrayList<Contributor>();

    public void setStaticInfoDao(StaticInfoDao staticInfoDao) {
        this.staticInfoDao = staticInfoDao;
    }

    public List<Partner> getPartnerCache() {
        return partnerCache;
    }

    public List<Contributor> getContributorCache() {
        return contributorCache;
    }

    public void refresh() {
        partnerCache = getPartnerData();
        contributorCache = getContributorData();
    }

    private List<Contributor> getContributorData() {
        return staticInfoDao.getAllContributors();
    }

    private List<Partner> getPartnerData() {
        return staticInfoDao.getAllPartnerItems();
    }


}