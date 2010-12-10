/*
 * Copyright 2007 EDL FOUNDATION
 *
 *  Licensed under the EUPL, Version 1.0 or as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  you may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  http://ec.europa.eu/idabc/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 */

package eu.europeana.web.controller;

import eu.europeana.core.database.UserDao;
import eu.europeana.core.database.domain.Role;
import eu.europeana.core.database.domain.SavedItem;
import eu.europeana.core.database.domain.SavedSearch;
import eu.europeana.core.database.domain.SocialTag;
import eu.europeana.core.database.domain.User;
import eu.europeana.core.querymodel.query.DocType;
import eu.europeana.core.util.indexing.SorlIndexUtil;
import eu.europeana.core.util.web.ClickStreamLogger;
import eu.europeana.core.util.web.ControllerUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;

import static eu.europeana.core.util.web.ClickStreamLogger.UserAction;

/**
 * General Controller for all AJAX actions
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@Controller
public class UserAjaxController {
    protected Logger log = Logger.getLogger(getClass());

    @Autowired
    private UserDao userDao;

    @Autowired
    private ClickStreamLogger clickStreamLogger;

    @Autowired
    private SorlIndexUtil sorlIndexUtil;

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleException(HttpServletRequest request, Exception exception) {
        return failure(request, exception);
    }

    @RequestMapping("/list-users.ajax")
    public ModelAndView listUsers() throws Exception {
        User user = ControllerUtil.getUser();
        if (user != null && (user.getRole() == Role.ROLE_ADMINISTRATOR || user.getRole() == Role.ROLE_GOD)) {
            ModelAndView page = complete(true);
            page.addObject("users", userDao.fetchUsers());
            return page;
        }
        else {
            throw new Exception("Not authorized");
        }
    }

    @RequestMapping("/remove-user.ajax")
    public ModelAndView removeUser(
            HttpServletRequest request,
            @RequestParam String email
    ) throws Exception {
        User user = userDao.fetchUserByEmail(email);
        if (user != null) {
            userDao.removeUser(user);
        }
        return complete(user != null);
    }

    @RequestMapping("/remove-saved-item.ajax")
    public ModelAndView removeSavedItem(
            HttpServletRequest request,
            @RequestParam Long id
    ) throws Exception {
        ControllerUtil.setUser(userDao.removeSavedItem(id));
        clickStreamLogger.logUserAction(request, UserAction.REMOVE_SAVED_ITEM);
        return complete(true);
    }


    @RequestMapping("/remove-saved-search.ajax")
    public ModelAndView removeSavedSearch(
            HttpServletRequest request,
            @RequestParam Long id
    ) throws Exception {
        ControllerUtil.setUser(userDao.removeSavedSearch(id));
        clickStreamLogger.logUserAction(request, UserAction.REMOVE_SAVED_SEARCH);
        return complete(true);
    }

    @RequestMapping("/remove-social-tag.ajax")
    public ModelAndView removeSocialTag(
            HttpServletRequest request,
            @RequestParam Long id
    ) throws Exception {
        final String europeanaUri = userDao.findEuropeanaUri(id);
        ControllerUtil.setUser(userDao.removeSocialTag(id));
        sorlIndexUtil.indexUserTags(europeanaUri);
        clickStreamLogger.logUserAction(request, UserAction.REMOVE_SOCIAL_TAG);
        return complete(true);
    }

    @RequestMapping("/save-saved-item.ajax")
    public ModelAndView saveSavedItem(
            HttpServletRequest request,
            @RequestParam String title,
            @RequestParam String author,
            @RequestParam String docType,
            @RequestParam String europeanaObject,
            @RequestParam String europeanaUri
    ) throws Exception {
        SavedItem savedItem = new SavedItem();
        savedItem.setTitle(title);
        savedItem.setAuthor(author);
        savedItem.setDocType(DocType.valueOf(docType));
        savedItem.setLanguage(ControllerUtil.getLocale(request));
        savedItem.setEuropeanaObject(europeanaObject);
        User user = ControllerUtil.getUser();
        ControllerUtil.setUser(userDao.addSavedItem(user, savedItem, europeanaUri));
        clickStreamLogger.logUserAction(request, UserAction.SAVE_ITEM);
        return complete(true);
    }

    @RequestMapping("/save-saved-search.ajax")
    public ModelAndView saveSavedSearch(
            HttpServletRequest request,
            @RequestParam String query,
            @RequestParam String queryString
    ) throws Exception {
        SavedSearch savedSearch = new SavedSearch();
        savedSearch.setQuery(query);
        savedSearch.setQueryString(URLDecoder.decode(queryString, "utf-8"));
        savedSearch.setLanguage(ControllerUtil.getLocale(request));
        User user = ControllerUtil.getUser();
        ControllerUtil.setUser(userDao.addSavedSearch(user, savedSearch));
        clickStreamLogger.logUserAction(request, UserAction.SAVE_SEARCH);
        return complete(true);
    }

    @RequestMapping("/save-social-tag.ajax")
    public ModelAndView saveSocialTag(
            HttpServletRequest request,
            @RequestParam String tag,
            @RequestParam String europeanaUri,
            @RequestParam String docType,
            @RequestParam String europeanaObject,
            @RequestParam String title
    ) throws Exception {
        SocialTag socialTag = new SocialTag();
        socialTag.setTag(tag);
        socialTag.setEuropeanaUri(europeanaUri);
        socialTag.setDocType(DocType.valueOf(docType));
        socialTag.setEuropeanaObject(europeanaObject);
        socialTag.setTitle(title);
        socialTag.setLanguage(ControllerUtil.getLocale(request));
        User user = ControllerUtil.getUser();
        ControllerUtil.setUser(userDao.addSocialTag(user, socialTag));
        boolean success = sorlIndexUtil.indexUserTags(europeanaUri);
        clickStreamLogger.logCustomUserAction(request, UserAction.SAVE_SOCIAL_TAG, "tag=" + tag);
        clickStreamLogger.logUserAction(request, UserAction.SAVE_SEARCH);
        return complete(true);
    }

    private ModelAndView failure(HttpServletRequest request, Exception e) {
        ModelAndView page = ControllerUtil.createModelAndViewPage("xml/ajax");
        page.addObject("success", false);
        page.addObject("exception", getStackTrace(e));
        clickStreamLogger.logUserAction(request, UserAction.AJAX_ERROR);
        log.warn("Problem handling AJAX request", e);
        return page;
    }

    private static ModelAndView complete(boolean success) {
        ModelAndView page = ControllerUtil.createModelAndViewPage("xml/ajax");
        page.addObject("success", success);
        return page;
    }

    private static String getStackTrace(Exception exception) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
