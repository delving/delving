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

package eu.europeana.controller;

import eu.europeana.database.dao.DashboardDao;
import eu.europeana.database.domain.CarouselItem;
import eu.europeana.database.domain.SavedItem;

/**
 * When an editor wants to associate a saved search or saved item with the carousel and proposed search
 * term, i.e. 'people are currently thinking about'.
 *
 * @author Borys Omelayenko
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class SaveToCarouselController extends AbstractAjaxTriggerController {
    private DashboardDao dashboardDao;

    public void setDashboardDao(DashboardDao dashboardDao) {
        this.dashboardDao = dashboardDao;
    }

    SavedItem savedItem;
    boolean isInCarousel;


    @Override
    public void prepareHandling(Long id, Class clazz) throws Exception {
        // check if this item is in carousel
        savedItem = dashboardDao.fetchSavedItemById(id);
        isInCarousel = (savedItem.getCarouselItem() != null);
    }

    @Override
    public boolean handleCheck(Long id, Class clazz) throws Exception {
        // user wants to put this item into carousel
        if (!isInCarousel) {
            // add a carousel item
            try {
                CarouselItem newCarouselItem = dashboardDao.createCarouselItem(
                        savedItem.getEuropeanaId().getEuropeanaUri(),
                        savedItem.getId());
                if (newCarouselItem == null) {
                    throw new Exception("Filure: null carousel item created");
                }
            } catch (Exception e) {
                throw new Exception("Failed to add carousel item with database id " + savedItem.getId(), e);
            }
        }
        return true;
    }

    @Override
    public boolean handleUnCheck(Long id, Class clazz) throws Exception {
        // user wants to remove this item from carousel
        if (isInCarousel) {
            // remove  a carousel item
            dashboardDao.removeFromCarousel(savedItem);
        }
        return true;
    }


}