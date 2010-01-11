/*
 * Copyright 2007 EDL FOUNDATION
 *
 *  Licensed under the EUPL, Version 1.1 or as soon they
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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */
public class ConfigInterceptor extends HandlerInterceptorAdapter {

    @Value("#{europeanaProperties['piwik.enabled']}")
    private String piwikEnabled;

    @Value("#{europeanaProperties['piwik.jsUrl']}")
    private String piwikJsUrl;

    @Value("#{europeanaProperties['piwik_log_url']}")
    private String piwikLogUrl;

    @Value("#{europeanaProperties['debug']}")
    private String debug;

    @Value("#{europeanaProperties['cacheUrl']}")
    private String cacheUrl;

    @Value("#{europeanaProperties['message.static_pages']}")
    private String staticPagesSources;


    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        super.postHandle(httpServletRequest, httpServletResponse, o, modelAndView);

        if (Boolean.valueOf(piwikEnabled)) {
            modelAndView.addObject("piwik_js", piwikJsUrl);
            modelAndView.addObject("piwik_log_url", piwikLogUrl);
        }
        if (!modelAndView.getViewName().startsWith("redirect:")) {
            modelAndView.addObject("debug", Boolean.valueOf(debug));
            modelAndView.addObject("cacheUrl", cacheUrl);
            modelAndView.addObject("staticPagesSource", staticPagesSources);
        }
    }


}
