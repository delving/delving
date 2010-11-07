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

import eu.delving.core.util.MessageSourceRepo;
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
import eu.europeana.core.util.web.EmailSender;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.Locale;
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

    private static Logger LOG = Logger.getLogger(AjaxController.class);
    private static boolean DEBUG = false; // true logs the exception to the xml response

    @Autowired
    private UserDao userDao;

    @Autowired
    private ClickStreamLogger clickStreamLogger;

    @Autowired
    @Qualifier("emailSenderForSendToFriend")
    private EmailSender friendEmailSender;

    @Autowired
    private SorlIndexUtil sorlIndexUtil;

    @Autowired
    private MessageSourceRepo messageSourceRepo;

    @RequestMapping("/message.ajax")
    public ModelAndView setTranslation(
            @RequestParam String key,
            @RequestParam String content,
            HttpServletRequest request,
            HttpServletResponse response,
            Locale locale
    ) {
        boolean success = false;
        String exceptionString = "";
        try {
            if (!hasJavascriptInjection(request)) {
                User user = ControllerUtil.getUser();
                if (user != null && (user.getRole() == Role.ROLE_GOD || user.getRole() == Role.ROLE_ADMINISTRATOR)) {
                    messageSourceRepo.setTranslation(key, content, locale);
                    success = true;
                }
            }
        }
        catch (Exception e) {
            exceptionString = handleAjaxException(e, response, request);
        }
        return createResponsePage(DEBUG, success, exceptionString, response);
    }

    @RequestMapping("/remove.ajax")
    public ModelAndView handleAjaxRemoveRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        boolean success = false;
        String exceptionString = "";
        try {
            if (!hasJavascriptInjection(request)) {
                success = processAjaxRemoveRequest(request);
            }
        }
        catch (Exception e) {
            exceptionString = handleAjaxException(e, response, request);
        }
        return createResponsePage(DEBUG, success, exceptionString, response);
    }

    @RequestMapping("/save.ajax")
    public ModelAndView handleAjaxSaveRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        boolean success = false;
        String exceptionString = "";
        try {
            if (!hasJavascriptInjection(request)) {
                success = processAjaxSaveRequest(request);
            }
        }
        catch (Exception e) {
            exceptionString = handleAjaxException(e, response, request);
        }
        return createResponsePage(DEBUG, success, exceptionString, response);
    }

    @RequestMapping("/email-to-friend.ajax")
    public ModelAndView handleSendToAFriendHandler(HttpServletRequest request, HttpServletResponse response) throws Exception {
        boolean success = false;
        String exceptionString = "";
        try {
            if (!hasJavascriptInjection(request)) {
                success = processSendToAFriendHandler(request);
            }
        }
        catch (Exception e) {
            handleAjaxException(e, response, request);
        }
        return createResponsePage(DEBUG, success, exceptionString, response);
    }

    // private

    private boolean processAjaxSaveRequest(HttpServletRequest request) throws Exception {
        User user = ControllerUtil.getUser();
        String className = request.getParameter("className");
        String idString = request.getParameter("id");
        boolean success = true;
        if (className == null) {
            throw new IllegalArgumentException("Expected 'className' parameter!");
        }

        switch (findModifiable(className)) {
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
            case SOCIAL_TAG:
                SocialTag socialTag = new SocialTag();
                String tagValue = getStringParameter("tag", request);
                socialTag.setTag(tagValue);
                String europeanaUri = getStringParameter("europeanaUri", request);
                socialTag.setEuropeanaUri(europeanaUri);
                socialTag.setDocType(DocType.valueOf(getStringParameter("docType", request)));
                socialTag.setEuropeanaObject(getStringParameter("europeanaObject", request));
                socialTag.setTitle(getStringParameter("title", request));
                socialTag.setLanguage(ControllerUtil.getLocale(request));
                user = userDao.addSocialTag(user, socialTag);
                success = sorlIndexUtil.indexUserTags(europeanaUri);
                clickStreamLogger.logCustomUserAction(request, UserAction.SAVE_SOCIAL_TAG, "tag=" + tagValue);
                break;
            default:
                throw new IllegalArgumentException("Unhandled removable");
        }

        ControllerUtil.setUser(user);

        return success;
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
        model.put(EmailSender.TO_EMAIL, emailAddress);
        model.put(EmailSender.FROM_EMAIL, user.getEmail());
        friendEmailSender.sendEmail(model);
        clickStreamLogger.logUserAction(request, UserAction.SEND_EMAIL_TO_FRIEND);
        return true;
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
            case SAVED_ITEM:
                user = userDao.removeSavedItem(id);
                clickStreamLogger.logUserAction(request, UserAction.REMOVE_SAVED_ITEM);
                break;
            case SAVED_SEARCH:
                user = userDao.removeSavedSearch(id);
                clickStreamLogger.logUserAction(request, UserAction.REMOVE_SAVED_SEARCH);
                break;
            case SOCIAL_TAG:
                final String europeanaUri = userDao.findEuropeanaUri(id);
                user = userDao.removeSocialTag(id);
                sorlIndexUtil.indexUserTags(europeanaUri);
                clickStreamLogger.logUserAction(request, UserAction.REMOVE_SOCIAL_TAG);
                break;
            default:
                throw new IllegalArgumentException("Unhandled removable");
        }

        ControllerUtil.setUser(user);
        return true;
    }


    private String handleAjaxException(Exception e, HttpServletResponse response, HttpServletRequest request) {
        response.setStatus(400);
        clickStreamLogger.logUserAction(request, UserAction.AJAX_ERROR);
        LOG.warn("Problem handling AJAX request", e);
        return getStackTrace(e);
    }

    private static ModelAndView createResponsePage(boolean debug, boolean success, String exceptionString, HttpServletResponse response) {
        ModelAndView page = ControllerUtil.createModelAndViewPage("xml/ajax");
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
        SAVED_ITEM(SavedItem.class),
        SAVED_SEARCH(SavedSearch.class),
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
                LOG.warn("The request contains javascript so do not process this request");
                break;
            }
        }
        return hasJavascript;
    }

}