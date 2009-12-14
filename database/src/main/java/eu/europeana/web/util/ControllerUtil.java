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

package eu.europeana.web.util;

import eu.europeana.database.domain.Language;
import eu.europeana.database.domain.User;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * Utility methods for controllers
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
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
        if (principal instanceof UserDaoDetailsService.UserHolder) {
            UserDaoDetailsService.UserHolder userHolder = (UserDaoDetailsService.UserHolder) authentication.getPrincipal();
            return userHolder.getUser();
        } else {
            return null;
        }
    }

    public static void setUser(User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            UserDaoDetailsService.UserHolder userHolder = (UserDaoDetailsService.UserHolder) authentication.getPrincipal();
            userHolder.setUser(user);
        }
    }

    public static String getServletUrl(HttpServletRequest request) {
        String url = request.getRequestURL().toString();
        int index = url.indexOf(request.getServerName());
        url = url.substring(0, index) + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        return url;
    }

    public static int getStartRow(HttpServletRequest request) {
        String startValue = request.getParameter("start");
        if (startValue == null) {
            startValue = request.getParameter("startRecord");
        }
        return getShiftedStartRow(startValue);
    }

    public static int getRows(HttpServletRequest request) {
        String rowsString = getParameter(request, "rows");
        if (rowsString == null) {
            rowsString = getParameter(request, "maximumRecords");
        }
        if (rowsString != null) {
            return Integer.parseInt(rowsString);
        } else {
            return -1;
        }
    }

    public static String getParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null || value.trim().length() == 0) {
            return null;
        }
        return value;
    }

    public static String getExpectedParameter(HttpServletRequest request, String name) {
        String value = getParameter(request, name);
        if (value == null) {
            throw new IllegalArgumentException("Expected parameter " + name);
        }
        return value;
    }

    private static int getShiftedStartRow(String startRowParameter) {
        if (startRowParameter != null) {
            try {
                int s = Integer.parseInt(startRowParameter);
                return s - 1;
            }
            catch (NumberFormatException nfe) {
                // ignore;
            }
        }
        return 0;
    }

    // todo: finish code for prerendering freemarker formatted strings with messageTags

    public static String getFreemarkerFormattedString(String input) throws IOException {
        Configuration configuration = new Configuration();
        configuration.setTemplateLoader(new ClassTemplateLoader(FreeMarkerConfigurer.class, ""));
        Reader reader = new StringReader("<#import \"/spring.ftl\" as spring />" + input);
        Template template = new Template("staticPageTemplate", reader, configuration, "utf-8");
        Writer out = new StringWriter();
        ModelAndView modelAndView = new ModelAndView();
        try {
            template.process(modelAndView, out);
        } catch (TemplateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toString();
    }

    public static Language getLocale(HttpServletRequest request) {
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        Locale locale = localeResolver.resolveLocale(request);
        String currentLangCode = locale.getLanguage();
        return Language.findByCode(currentLangCode);
    }

    /*
     * This creates the default ModelAndView for the portal applications. It should be used in every Controller.
     */

    public static ModelAndView createModelAndViewPage(String view) {
        ModelAndView page = new ModelAndView(view);
        User user = ControllerUtil.getUser();
        page.addObject("user", user);
        return page;
    }

    /*
    * Format full requested uri from HttpServletRequest
    */
    public static String formatFullRequestUrl(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        if (request.getQueryString() != null) {
            requestURL.append("?").append(request.getQueryString());
        } else if (request.getParameterMap() != null) {
            requestURL.append(formatParameterMapAsQueryString(request.getParameterMap()));
        }
        return requestURL.toString();
    }

    @SuppressWarnings({"unchecked"})
    public static String formatParameterMapAsQueryString(Map parameterMap) {
        StringBuilder output = new StringBuilder();
        output.append("?");
        for (Iterator iterator1 = parameterMap.entrySet().iterator(); iterator1.hasNext();) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) iterator1.next();
            output.append(MessageFormat.format("{0}={1}", entry.getKey(), entry.getValue()));
            if (iterator1.hasNext()) {
                output.append("&");
            }
        }
        return output.toString();
    }

}