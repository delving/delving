package eu.europeana.query;

import eu.europeana.web.util.ResultPagination;
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
 *         QueryConstraints=
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

public interface RequestLogger {

    void log(HttpServletRequest request, HttpServletResponse servletResponse, UserActions actions);

    void log(HttpServletRequest request, ModelAndView model, UserActions actions);

    void log(HttpServletRequest request, ResultModel resultModel, QueryModel queryModel,
             ResultPagination resultPagination, ModelAndView model, UserActions actions);

    void log(HttpServletRequest request, UserActions actions);

    /**
     * Enum for different user actions that can be logged.
     */
    public enum UserActions {
        // todo: add descriptions
        LANGUAGE_CHANGE,
        BRIEF_RESULT,
        FULL_RESULT,
        REDIRECT_OUTLINK,
        STATICPAGE,
        TIMELINE,
        SAVE_ITEM,
        SAVE_SEARCH,
        SAVE_SOCIAL_TAG,
        REMOVE_ITEM,
        REMOVE_SEARCH,
        REMOVE_SOCIAL_TAG,
        REGISTER,
        UNREGISTER,
        ERROR,
        RETURN_TO_RESULTS,
        INDEXPAGE;

        private String description;

        UserActions() {
        }

        UserActions(String description) {
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


