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

import eu.delving.core.storage.UserRepo;
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
    private UserRepo userRepo;

    @Autowired
    private ClickStreamLogger clickStreamLogger;

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleException(HttpServletRequest request, Exception exception) {
        return failure(request, exception);
    }

    @RequestMapping("/list-users.ajax")
    public ModelAndView listUsers() throws Exception {
        UserRepo.Person person = ControllerUtil.getPerson();
        if (person != null && (person.getRole() == UserRepo.Role.ROLE_ADMINISTRATOR || person.getRole() == UserRepo.Role.ROLE_GOD)) {
            ModelAndView page = complete(true);
            page.addObject("users", userRepo.getPeople());
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
        UserRepo.Person person = userRepo.byEmail(email);
        if (person != null) {
            person.delete();
        }
        return complete(person != null);
    }

    @RequestMapping("/remove-saved-item.ajax")
    public ModelAndView removeSavedItem(
            HttpServletRequest request,
            @RequestParam Long id
    ) throws Exception {
        ControllerUtil.getPerson().getItems();
        // todo: find the item and remove it
        clickStreamLogger.logUserAction(request, UserAction.REMOVE_SAVED_ITEM);
        return complete(true);
    }


    @RequestMapping("/remove-saved-search.ajax")
    public ModelAndView removeSavedSearch(
            HttpServletRequest request,
            @RequestParam Long id
    ) throws Exception {
        ControllerUtil.getPerson().getSearches();
        // todo: find the search and remove it.
        clickStreamLogger.logUserAction(request, UserAction.REMOVE_SAVED_SEARCH);
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
//        savedItem.setDocType(DocType.valueOf(docType));
//        savedItem.setEuropeanaObject(europeanaObject);
        UserRepo.Person person = ControllerUtil.getPerson();
        person.addItem(author, title, ControllerUtil.getLocale(request));
        person.save();
        clickStreamLogger.logUserAction(request, UserAction.SAVE_ITEM);
        return complete(true);
    }

    @RequestMapping("/save-saved-search.ajax")
    public ModelAndView saveSavedSearch(
            HttpServletRequest request,
            @RequestParam String query,
            @RequestParam String queryString
    ) throws Exception {
        UserRepo.Person person = ControllerUtil.getPerson();
        person.addSearch(query, URLDecoder.decode(queryString, "utf-8"), ControllerUtil.getLocale(request));
        person.save();
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
