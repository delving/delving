package eu.europeana.web.util;

import javax.servlet.http.HttpServletRequest;

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

public interface RequestLogger {

    void log(HttpServletRequest request);

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


