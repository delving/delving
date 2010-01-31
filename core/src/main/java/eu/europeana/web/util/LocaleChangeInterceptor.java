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
import eu.europeana.query.ClickStreamLogger;
import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;


public class LocaleChangeInterceptor extends HandlerInterceptorAdapter {

    public static final String DEFAULT_PARAM_NAME = "locale";

    private String paramName = DEFAULT_PARAM_NAME;
    private ClickStreamLogger clickStreamLogger;

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamName() {
        return this.paramName;
    }

    public void setClickStreamLogger(ClickStreamLogger clickStreamLogger) {
        this.clickStreamLogger = clickStreamLogger;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws ServletException {
        Language oldLocale = ControllerUtil.getLocale(request);
        String newLocale = request.getParameter(this.paramName);
        if (newLocale != null) {
            LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
            if (localeResolver == null) {
                throw new IllegalStateException("No LocaleResolver found: not in a DispatcherServlet request?");
            }
            LocaleEditor localeEditor = new LocaleEditor();
            localeEditor.setAsText(newLocale);
            localeResolver.setLocale(request, response, (Locale) localeEditor.getValue());
            clickStreamLogger.log(request, oldLocale, ClickStreamLogger.UserAction.LANGUAGE_CHANGE);
        }
        // Proceed in any case.
        return true;
    }

}

