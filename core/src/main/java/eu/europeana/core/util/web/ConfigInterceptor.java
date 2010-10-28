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

package eu.europeana.core.util.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;
import java.util.Map;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */
public class ConfigInterceptor extends HandlerInterceptorAdapter {

    @Value("#{launchProperties['debug']}")
    private String debug;

    @Value("#{launchProperties['cacheUrl']}")
    private String cacheUrl;

    @Value("#{launchProperties['portal.name']}")
    private String portalName;

    @Value("#{launchProperties['portal.displayName']}")
    private String portalDisplayName;


    @Value("#{launchProperties['portal.theme']}")
    private String portalTheme;

    @Value("#{launchProperties['portal.color']}")
    private String portalColor;

    @Value("#{launchProperties['ga.trackingCode']}")
    private String googleAnalyticsTrackingCode;

    @Value("#{launchProperties['addThis.trackingCode']}")
    private String addThisTrackingCode;

    @SuppressWarnings({"unchecked"})
    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        super.postHandle(httpServletRequest, httpServletResponse, o, modelAndView);
        if (modelAndView != null && !modelAndView.getViewName().startsWith("redirect:")) {
            modelAndView.addObject("debug", Boolean.valueOf(debug));
            modelAndView.addObject("interfaceLanguage", ControllerUtil.getLocale(httpServletRequest));
            modelAndView.addObject("cacheUrl", cacheUrl);
            modelAndView.addObject("portalName", portalName);
            modelAndView.addObject("portalDisplayName", portalDisplayName);
            modelAndView.addObject("portalTheme", portalTheme);
            modelAndView.addObject("portalColor", portalColor);
            modelAndView.addObject("defaultParams", getDefaultParameters(httpServletRequest.getParameterMap()));
            if (!googleAnalyticsTrackingCode.isEmpty()) {
                modelAndView.addObject("googleAnalyticsTrackingCode", googleAnalyticsTrackingCode);
            }
            if (!addThisTrackingCode.isEmpty()) {
                modelAndView.addObject("addThisTrackingCode", addThisTrackingCode);
            }
        }
    }

    private String getDefaultParameters(Map<String, String[]> params) {
        StringBuilder out = new StringBuilder();
        out.append(getKey("view", params));
        out.append(getKey("tab", params));
        out.append(getKey("sortBy", params));
        out.append(getKey("sortOrder", params));
        return out.toString();
    }

    private String getKey(String key, Map<String, String[]> params) {
        if (params.containsKey(key) && !params.get(key)[0].isEmpty()) {
            return MessageFormat.format("&%s=%s", key, params.get(key)[0]);
        }
        return "";
    }
}
