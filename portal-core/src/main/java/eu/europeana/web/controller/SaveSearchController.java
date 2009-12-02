package eu.europeana.web.controller;

import eu.europeana.database.UserDao;
import eu.europeana.database.domain.SavedSearch;
import eu.europeana.database.domain.User;
import eu.europeana.web.util.ControllerUtil;
import javax.servlet.http.HttpServletRequest;

import java.net.URLDecoder;

/**
 * When somebody wants to save a search they've done for later.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class SaveSearchController extends AbstractAjaxController {
    private UserDao userDao;

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public boolean handleAjax(HttpServletRequest request) throws Exception {
        SavedSearch savedSearch = new SavedSearch();
        savedSearch.setQuery(getStringParameter("query",request));
        savedSearch.setQueryString(URLDecoder.decode(getStringParameter("queryString", request), "utf-8"));
        savedSearch.setLanguage(ControllerUtil.getLocale(request));
        User user = ControllerUtil.getUser();
        user = userDao.addSavedSearch(user, savedSearch);
        ControllerUtil.setUser(user);
        return true;
    }
}