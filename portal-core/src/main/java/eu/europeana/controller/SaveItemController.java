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

package eu.europeana.controller;

import eu.europeana.controller.util.ControllerUtil;
import eu.europeana.database.UserDao;
import eu.europeana.database.domain.User;
import eu.europeana.database.domain.SavedItem;
import eu.europeana.query.DocType;

import javax.servlet.http.HttpServletRequest;

/**
 * When somebody wants to save an item they have found.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class SaveItemController extends AbstractAjaxController {
    private UserDao userDao;

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public boolean handleAjax(HttpServletRequest request) throws Exception {
        User user = ControllerUtil.getUser();
        // add the required fields
        SavedItem savedItem = new SavedItem();
        savedItem.setTitle(getStringParameter("title", request));
        savedItem.setAuthor(getStringParameter("author", request));
        savedItem.setDocType(DocType.valueOf(getStringParameter("docType", request)));
        savedItem.setEuropeanaObject(getStringParameter("europeanaObject", request));
        user = userDao.addSavedItem(user, savedItem, getStringParameter("europeanaUri", request));
        ControllerUtil.setUser(user);
        return true;
    }
}