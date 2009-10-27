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

import eu.europeana.database.DashboardDao;
import eu.europeana.database.domain.EditorPick;
import eu.europeana.database.domain.SavedSearch;

/**
 * When an editor wants to associate a saved search or saved item with the carousel and proposed search
 * term, i.e. 'people are currently thinking about'.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Borys Omelayenko
 */

public class SaveEditorPickController extends AbstractAjaxTriggerController {
    private DashboardDao dashboardDao;

    public void setDashboardDao(DashboardDao dashboardDao) {
        this.dashboardDao = dashboardDao;
    }

    SavedSearch savedSearch;
    boolean isInPacta;

		@Override
		public void prepareHandling(Long id, Class clazz) throws Exception {
      // check if this item is in pacta
      savedSearch = dashboardDao.fetchSavedSearchById(id);
      isInPacta = (savedSearch.getEditorPick() != null);
		}

		@Override
		public boolean handleCheck(Long id, Class clazz) throws Exception {
      // user wants to put this item into pacta
      if (!isInPacta) {
          // add a carousel item
          try {
          EditorPick newEditorPick = dashboardDao.createEditorPick(savedSearch);
          if (newEditorPick == null) {
          	throw new Exception("Filure: null editor pick created");
          }
          } catch (Exception e) {
              throw new Exception("Failed to add pacta item with query " + savedSearch.getQuery(), e);
          }
      }
      return true;
		}

		@Override
		public boolean handleUnCheck(Long id, Class clazz) throws Exception {
	     // user wants to remove this item from pacta
      if (isInPacta) {
          // remove  a carousel item
      	  dashboardDao.removeFromEditorPick(savedSearch);
      }
      return true;
		}
}