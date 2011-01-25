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

package eu.europeana.core.util.web;

import eu.delving.core.storage.SpringUserService;
import eu.delving.core.storage.User;
import eu.delving.domain.Language;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * Utility methods for controllers
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd@delving.eu>
 */

public class ControllerUtil {
    private static final String EMAIL_REGEXP = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[_A-Za-z0-9-]+)";

    public static boolean validEmailAddress(String emailAddress) {
        return emailAddress.matches(EMAIL_REGEXP);
    }

    public static User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof SpringUserService.UserHolder) {
            SpringUserService.UserHolder userHolder = (SpringUserService.UserHolder) authentication.getPrincipal();
            return userHolder.getUser();
        }
        else {
            return null;
        }
    }

    public static String getServletUrl(HttpServletRequest request) {
        String url = request.getRequestURL().toString();
        int index = url.indexOf(request.getServerName());
        url = url.substring(0, index) + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        return url;
    }

    public static String getFullServletUrl(HttpServletRequest request) {
        String url = request.getRequestURL().toString();
        int index = url.indexOf(request.getServerName());
        url = url.substring(0, index) + request.getServerName() + ":" + request.getServerPort() + request.getRequestURI();
        return url;
    }

    public static String getParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null || value.trim().length() == 0) {
            return null;
        }
        return value;
    }

    public static Language getLocale(HttpServletRequest request) {
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        Locale locale = localeResolver.resolveLocale(request);
        String currentLangCode = locale.getLanguage();
        return Language.findByCode(currentLangCode);
    }

    /**
     * This creates the default ModelAndView for the portal applications. It should be used in every Controller.
     *
     * @param view The Freemarker template that will be used by the model to render the view
     * @return ModelAndView page
     */

    public static ModelAndView createModelAndViewPage(String view) {
        ModelAndView page = new ModelAndView(view);
        User user = ControllerUtil.getUser();
        page.addObject("user", user);
        return page;
    }

    /**
     * This creates the default ModelAndView for the portal applications. It should be used in every Controller. When the
     * varArgs includes is not empty the view is interpreted as a Surrounding Template and however many includes values
     * are injected into the model as Freemarke variable 'include' + number increment, for example 'include1'. In a
     * Freemarker template that is designed as a Surrounding Template it needs to check for if an include exists,
     * for example:
     *
     * <p>
     *  <code>
     *      <#if include1??> <#include "${include1}.ftl"> <#/if>
     *  </code>
     * </p>
     *
     * @param view Surrounding Template
     * @param includes a VarArg with the includes that will injected into the model as 'include' + increment
     * @return ModelAndView page
     */

    public static ModelAndView createModelAndViewPage(String view, String... includes) {
        ModelAndView page = new ModelAndView(view);
        User user = ControllerUtil.getUser();
        page.addObject("user", user);
        for (int i = 0; i < includes.length; i++) {
            String include = includes[i];
            if (!include.isEmpty()) {
                page.addObject(String.format("include%d", i), include);
            }
        }
        return page;
    }

    /*
    * Format full requested uri from HttpServletRequest
    */
    @SuppressWarnings("unchecked")
    public static String formatFullRequestUrl(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        if (request.getQueryString() != null) {
            requestURL.append("?").append(request.getQueryString());
        }
        else if (request.getParameterMap() != null) {
            requestURL.append(formatParameterMapAsQueryString(request.getParameterMap()));
        }
        return requestURL.toString();
    }

    public static String formatParameterMapAsQueryString(Map<String, String[]> parameterMap) {
        StringBuilder output = new StringBuilder();
        output.append("?");
        Iterator<Map.Entry<String, String[]>> iterator1 = parameterMap.entrySet().iterator();
        while (iterator1.hasNext()) {
            Map.Entry<String, String[]> entry = iterator1.next();
            if (entry.getValue().length > 0) {
                output.append(MessageFormat.format("{0}={1}", entry.getKey(), entry.getValue()[0]));
            }
            else {
                output.append(MessageFormat.format("{0}={1}", entry.getKey(), ""));
            }
            if (iterator1.hasNext()) {
                output.append("&");
            }
        }
        return output.toString();
    }

}
