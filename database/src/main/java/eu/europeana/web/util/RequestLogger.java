package eu.europeana.web.util;

import eu.europeana.database.domain.User;
import eu.europeana.query.QueryExpression;
import eu.europeana.query.QueryModel;
import eu.europeana.query.ResultModel;
import eu.europeana.web.controller.IndexController;
import eu.europeana.web.controller.StaticPageController;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;
import java.util.Date;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 *
 * This Class is used add custom logging information to the application log.
 * These logs are used to trace user behavior with special focus for how to best
 * provide multilingual support of  the Europeana Users.
 *
 *
 * The idea is to log
 * // [idPhd=1, userId="123", query="sjoerd", queryType="", constrains="country=, " ip=""]
        /*
        *  logTypeId=""
        *  userId=""
        *  query=""
        *  queryType=
        *  language=
        *  QueryConstraints=
        *  date=
        *  ip=
        *  pageId=
        *  state=
        *  pageNr=
        *  NrResults=
        *  LanguageFacets="en(12), de(9), fi(1)"
        *  CountryFacet="de(14), ..."
        *  templateName="" -.ftl
        *
        *  actions=[languageChange, Result, staticPage, saveItem, saveSearch, SaveTag, register, Error, ReturnResults]
        *
 */

public class RequestLogger {

    private Logger log = Logger.getLogger(getClass());

    private LogTypeId logTypeId;
    private String query; //
    private QueryExpression.QueryType queryType;
    private String queryConstraints; // a comma separated list of qf's from url.
    private String pageId;
    // private String state;
    private int pageNr;
    private int nrResults;
    private String languageFacets;
    private String countryFacet;
    private UserActions actions;

    public RequestLogger(LogTypeId logTypeId, UserActions actions) {
        this.logTypeId = logTypeId;
        this.actions = actions;
    }

    public RequestLogger(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) {
        // determine the Class
        final Class<? extends Object> aClass = o.getClass();
        if (aClass == StaticPageController.class) {
            actions = UserActions.STATICPAGE;
        } else if (aClass == IndexController.class) {
            actions = UserActions.INDEXPAGE;
        }
    }

    // this constructor is for search Controllers
    public RequestLogger(ResultModel resultModel, QueryModel queryModel, ResultPagination resultPagination, Class<? extends Object> aClass) {

        nrResults = resultPagination.getNumFound();

        // get elements from queryModel
        query = queryModel.getQueryString();
        queryType = queryModel.getQueryType();
        queryConstraints = formatQueryConstraints(queryModel.getConstraints());


    }


    private String formatQueryConstraints(QueryModel.Constraints constraints) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    /*
    * This method will be called in configInterceptor and will take some info form
    * request and modelAndView, format the log entry, and write it to log.
    */
    public void writeRequestLog (HttpServletRequest request, ModelAndView modelAndView) {
        if (modelAndView.getViewName() != null) {
            Date date = new Date();
            String ip = request.getRemoteAddr();
            String templateName = modelAndView.getViewName();
            final User user = ControllerUtil.getUser();
            String userId;
            if (user != null) {
                userId = user.getId().toString();
            } else {
                userId = "";
            }
            String language = ControllerUtil.getLocale(request).toString();
            log.info(MessageFormat.format("[userId={0}, lang={1}, date={2}, template={3}, ip={4}]", userId, language, date, templateName, ip));
        }
    }


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
       PHD_JS("Julianne Stillers custom log format"),
       PHD_MS("Maria ??? custom log format");

       private String description;

       LogTypeId(String description) {
           this.description = description;
       }

       public String getDescription() {
           return description;
       }
   }

}


