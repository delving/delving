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

package eu.europeana.query;

import eu.europeana.database.domain.StaticPageType;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 *         <p/>
 *         This interface is used add custom logging information to the application log.
 *         These logs are used to trace user behavior with special focus for how to best
 *         provide multilingual support of  the Europeana Users.
 *         <p/>
 *         <p/>
 *         The idea is to log
 *         // [idPhd=1, userId="123", query="sjoerd", queryType="", constrains="country=, " ip=""]
 *         /*
 *         logTypeId=""
 *         userId=""
 *         query=""
 *         queryType=
 *         language=
 *         date=
 *         ip=
 *         pageId=
 *         state=
 *         pageNr=
 *         NrResults=
 *         LanguageFacets="en(12), de(9), fi(1)"
 *         CountryFacet="de(14), ..."
 *         templateName="" -.ftl
 *         <p/>
 *         actions=[languageChange, Result, staticPage, saveItem, saveSearch, SaveTag, register, Error, ReturnResults]
 */

public interface ClickStreamLogger {

    void log(HttpServletRequest request, HttpServletResponse servletResponse, UserAction action);

    void log(HttpServletRequest request, UserAction action, ModelAndView model);

//    void log(HttpServletRequest request, ResultModel resultModel, QueryModel queryModel,
//             ResultPagination resultPagination, ModelAndView model, UserAction action);

    void log(HttpServletRequest request, UserAction action);

    void log(HttpServletRequest request, StaticPageType pageType);

    /**
     * Enum for different user actions that can be logged.
     */
    public enum UserAction {
        // todo: add descriptions
        // language specific actions
        LANGUAGE_CHANGE,

        // search related actions
        BRIEF_RESULT,
        BRIEF_RESULT_BROWSE,
        FULL_RESULT,
        MORE_LIKE_THIS,
        RETURN_TO_RESULTS,
        REDIRECT_OUTLINK,
        TIMELINE,
        TAG_GRID,
        YEAR_GRID,
        BROWSE_BOB,

        // ajax related actions
        SAVE_ITEM,
        SAVE_SEARCH,
        SAVE_SOCIAL_TAG,
        SAVE_CAROUSEL_ITEM,
        SAVE_SEARCH_TERM,
        REMOVE_SAVED_ITEM,
        REMOVE_SAVED_SEARCH,
        REMOVE_SOCIAL_TAG,
        REMOVE_CAROUSEL_ITEM,
        REMOVE_SEARCH_TERM,
        SEND_EMAIL_TO_FRIEND,
        SEND_FEEDBACK,
        TAG_AUTOCOMPLETE,

        // user management related actions
        REGISTER,
        UNREGISTER,
        LOGIN,
        LOGOUT,

        // errors
        ERROR,
        AJAX_ERROR,

        // static pages
        STATICPAGE,
        INDEXPAGE;

        private String description;

        UserAction() {
        }

        UserAction(String description) {
            this.description = description;
        }
    }

    public enum LogTypeId {
        PHD_JS("Juliane Stiller's custom log format"),
        PHD_MG("Maria Gäde's custom log format");

        private String description;

        LogTypeId(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

}


