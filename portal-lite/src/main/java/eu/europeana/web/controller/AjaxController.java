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

import eu.europeana.core.database.StaticInfoDao;
import eu.europeana.core.database.UserDao;
import eu.europeana.core.database.domain.*;
import eu.europeana.core.querymodel.query.DocType;
import eu.europeana.core.util.web.ClickStreamLogger;
import eu.europeana.core.util.web.ControllerUtil;
import eu.europeana.core.util.web.EmailSender;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static eu.europeana.core.util.web.ClickStreamLogger.UserAction;

/**
 * General Controller for all AJAX actions
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@Controller
public class AjaxController {

    protected Logger log = Logger.getLogger(getClass());
    private boolean debug = false; // true logs the exception to the xml response
    private boolean success = false;
    private String exceptionString = "";

    @Autowired
    private UserDao userDao;

    @Autowired
    private StaticInfoDao staticInfoDao;

    @Autowired
    private ClickStreamLogger clickStreamLogger;

    @Autowired
    @Qualifier("emailSenderForSendToFriend")
    private EmailSender friendEmailSender;

    @RequestMapping("/remove.ajax")
    public ModelAndView handleAjaxRemoveRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            if (!hasJavascriptInjection(request)) {
                success = processAjaxRemoveRequest(request);
            }
        }
        catch (Exception e) {
            handleAjaxException(e, response, request);
        }
        return createResponsePage(debug, success, exceptionString, response);
    }

    private Boolean processAjaxRemoveRequest(HttpServletRequest request) throws Exception {
        String className = request.getParameter("className");
        String idString = request.getParameter("id");
        if (className == null || idString == null) {
            throw new IllegalArgumentException("Expected 'className' and 'id' parameters!");
        }
        Long id = Long.valueOf(idString);

        User user;
        switch (findModifiable(className)) {
            case CAROUSEL_ITEM:
                user = staticInfoDao.removeCarouselItemFromSavedItem(id);
                clickStreamLogger.logUserAction(request, UserAction.REMOVE_CAROUSEL_ITEM);
                break;
            case SAVED_ITEM:
                user = userDao.removeSavedItem(id);
                clickStreamLogger.logUserAction(request, UserAction.REMOVE_SAVED_ITEM);
                break;
            case SAVED_SEARCH:
                user = userDao.removeSavedSearch(id);
                clickStreamLogger.logUserAction(request, UserAction.REMOVE_SAVED_SEARCH);
                break;
            case SEARCH_TERM:
                user = staticInfoDao.removeSearchTerm(id);
                clickStreamLogger.logUserAction(request, UserAction.REMOVE_SEARCH_TERM);
                break;
            case SOCIAL_TAG:
                user = userDao.removeSocialTag(id);
                clickStreamLogger.logUserAction(request, UserAction.REMOVE_SOCIAL_TAG);
                break;
            default:
                throw new IllegalArgumentException("Unhandled removable");
        }

        ControllerUtil.setUser(user);
        return true;
    }

    @RequestMapping("/save.ajax")
    public ModelAndView handleAjaxSaveRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            if (!hasJavascriptInjection(request)) {
                success = processAjaxSaveRequest(request);
            }
        }
        catch (Exception e) {
            handleAjaxException(e, response, request);
        }
        return createResponsePage(debug, success, exceptionString, response);
    }

    private boolean processAjaxSaveRequest(HttpServletRequest request) throws Exception {
        User user = ControllerUtil.getUser();
        String className = request.getParameter("className");
        String idString = request.getParameter("id");
        if (className == null) {
            throw new IllegalArgumentException("Expected 'className' parameter!");
        }

        switch (findModifiable(className)) {
            case CAROUSEL_ITEM:
                SavedItem savedItemForCarousel = userDao.fetchSavedItemById(Long.valueOf(idString));
                CarouselItem carouselItem = staticInfoDao.createCarouselItem(savedItemForCarousel.getId());
                if (carouselItem == null) {
                    return false;
                }
                clickStreamLogger.logUserAction(request, UserAction.SAVE_CAROUSEL_ITEM);
                break;
            case SAVED_ITEM:
                SavedItem savedItem = new SavedItem();
                savedItem.setTitle(getStringParameter("title", request));
                savedItem.setAuthor(getStringParameter("author", request));
                savedItem.setDocType(DocType.valueOf(getStringParameter("docType", request)));
                savedItem.setLanguage(ControllerUtil.getLocale(request));
                savedItem.setEuropeanaObject(getStringParameter("europeanaObject", request));
                user = userDao.addSavedItem(user, savedItem, getStringParameter("europeanaUri", request));
                clickStreamLogger.logUserAction(request, UserAction.SAVE_ITEM);
                break;
            case SAVED_SEARCH:
                SavedSearch savedSearch = new SavedSearch();
                savedSearch.setQuery(getStringParameter("query", request));
                savedSearch.setQueryString(URLDecoder.decode(getStringParameter("queryString", request), "utf-8"));
                savedSearch.setLanguage(ControllerUtil.getLocale(request));
                user = userDao.addSavedSearch(user, savedSearch);
                clickStreamLogger.logUserAction(request, UserAction.SAVE_SEARCH);
                break;
            case SEARCH_TERM:
                SearchTerm searchTerm = staticInfoDao.addSearchTerm(Long.valueOf(idString));
                if (searchTerm == null) {
                    return false;
                }
                clickStreamLogger.logUserAction(request, UserAction.SAVE_SEARCH_TERM);
                break;
            case SOCIAL_TAG:
                SocialTag socialTag = new SocialTag();
                String tagValue = getStringParameter("tag", request);
                socialTag.setTag(tagValue);
                socialTag.setEuropeanaUri(getStringParameter("europeanaUri", request));
                socialTag.setDocType(DocType.valueOf(getStringParameter("docType", request)));
                socialTag.setEuropeanaObject(getStringParameter("europeanaObject", request));
                socialTag.setTitle(getStringParameter("title", request));
                socialTag.setLanguage(ControllerUtil.getLocale(request));
                user = userDao.addSocialTag(user, socialTag);
                clickStreamLogger.logCustomUserAction(request, UserAction.SAVE_SOCIAL_TAG, "tag="+tagValue);
                break;
            default:
                throw new IllegalArgumentException("Unhandled removable");
        }

        ControllerUtil.setUser(user);
        return true;
    }

    @RequestMapping("/email-to-friend.ajax")
    public ModelAndView handleSendToAFriendHandler(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            if (!hasJavascriptInjection(request)) {
                success = processSendToAFriendHandler(request);
            }
        }
        catch (Exception e) {
            handleAjaxException(e, response, request);
        }
        return createResponsePage(debug, success, exceptionString, response);
    }

    private boolean processSendToAFriendHandler(HttpServletRequest request) throws Exception {
        String emailAddress = getStringParameter("email", request);
        if (!ControllerUtil.validEmailAddress(emailAddress)) {
            throw new IllegalArgumentException("Email address invalid: [" + emailAddress + "]");
        }
        String uri = getStringParameter("uri", request);
        User user = ControllerUtil.getUser();
        Map<String, Object> model = new TreeMap<String, Object>();
        model.put("user", user);
        model.put("uri", uri);
        model.put("email", emailAddress);
        String subject = "A link from Europeana"; // replace with injection later
        friendEmailSender.sendEmail(emailAddress, user.getEmail(), subject, model);
        clickStreamLogger.logUserAction(request, UserAction.SEND_EMAIL_TO_FRIEND);
        return true;
    }

    // currently not used. todo: maybe remove later
    @RequestMapping("/tag-autocomplete.ajax")
    public ModelAndView handleTagAutoCompleteRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("xml"); // todo: viewResolver should set content type
        String query = request.getParameter("q").toLowerCase();
        if (query == null) {
            query = "";
        }
        ModelAndView page = ControllerUtil.createModelAndViewPage("tag-autocomplete");
        try {
            List<UserDao.TagCount> tagCountList = userDao.getSocialTagCounts(query);
            page.addObject("tagList", tagCountList);
        }
        catch (Exception e) {
            handleAjaxException(e, response, request);
        }
        clickStreamLogger.logUserAction(request, UserAction.TAG_AUTOCOMPLETE);
        return page;
    }


    private void handleAjaxException(Exception e, HttpServletResponse response, HttpServletRequest request) {
        success = false;
        response.setStatus(400);
        exceptionString = getStackTrace(e);
        clickStreamLogger.logUserAction(request, UserAction.AJAX_ERROR);
        log.warn("Problem handling AJAX request", e);
    }

    private static ModelAndView createResponsePage(boolean debug, boolean success, String exceptionString, HttpServletResponse response) {
        ModelAndView page = ControllerUtil.createModelAndViewPage("ajax");
        response.setContentType("text/xml");  // todo: viewResolver should set content type
        page.addObject("success", String.valueOf(success));
        page.addObject("exception", exceptionString);
        page.addObject("debug", debug);
        return page;
    }

    private static Modifiable findModifiable(String className) {
        for (Modifiable modifiable : Modifiable.values()) {
            if (modifiable.matches(className)) {
                return modifiable;
            }
        }
        throw new IllegalArgumentException("Unable to find removable class with name " + className);
    }


    private enum Modifiable {
        CAROUSEL_ITEM(CarouselItem.class),
        SAVED_ITEM(SavedItem.class),
        SAVED_SEARCH(SavedSearch.class),
        SEARCH_TERM(SearchTerm.class),
        SOCIAL_TAG(SocialTag.class);

        private String className;

        private Modifiable(Class<?> clazz) {
            this.className = clazz.getName().substring(clazz.getName().lastIndexOf('.') + 1);
        }

        public boolean matches(String className) {
            return this.className.equals(className);
        }
    }


    protected static String getStringParameter(String parameterName, HttpServletRequest request) {
        String stringValue = request.getParameter(parameterName);
        if (stringValue == null) {
            throw new IllegalArgumentException("Missing parameter: " + parameterName);
        }
        stringValue = stringValue.trim();
        return stringValue;
    }

    private static String getStackTrace(Exception exception) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    // todo: write better javascript detection code

    private boolean hasJavascriptInjection(HttpServletRequest request) {
        boolean hasJavascript = false;
        Map map = request.getParameterMap();
        for (Object o : map.keySet()) {
            if (request.getParameter(String.valueOf(o)).contains("<")) {
                hasJavascript = true;
                log.warn("The request contains javascript so do not process this request");
                break;
            }
        }
        return hasJavascript;
    }

}