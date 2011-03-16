/*
 * Copyright 2011 DELVING BV
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.europeana.core.util.web;

import eu.delving.core.binding.FreemarkerUtil;
import eu.delving.core.binding.QueryParamList;
import eu.delving.core.util.PortalTheme;
import eu.delving.core.util.ThemeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */
public class ConfigInterceptor extends HandlerInterceptorAdapter {

    @Value("#{launchProperties['debug']}")
    private String debug;

    @Value("#{launchProperties['portal.name']}")
    private String portalName;

    @Resource(name = "includedMacros")
    private List<String> includedMacros;

    @Autowired
    private FreemarkerUtil freeMarkerUtil;

    @SuppressWarnings({"unchecked"})
    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        super.postHandle(httpServletRequest, httpServletResponse, o, modelAndView);
        final PortalTheme theme = ThemeInterceptor.getTheme();
        if (modelAndView != null && !modelAndView.getViewName().startsWith("redirect:")) {
            modelAndView.addObject("debug", Boolean.valueOf(debug));
            modelAndView.addObject("interfaceLanguage", ControllerUtil.getLocale(httpServletRequest));
            modelAndView.addObject("cacheUrl", theme.getCacheUrl());
            modelAndView.addObject("portalName", portalName);
            modelAndView.addObject("portalDisplayName", theme.getDisplayName());
            modelAndView.addObject("portalBaseUrl", theme.getBaseUrl());
            modelAndView.addObject("portalColor", theme.getColorScheme());
            modelAndView.addObject("portalTheme", "theme/" + theme.getName());
            final QueryParamList queryParamList = FreemarkerUtil.createQueryParamList(httpServletRequest.getParameterMap());
            modelAndView.addObject("queryParamList", queryParamList);
            modelAndView.addObject("defaultParams", queryParamList.getDefaultParamsFormatted());
            modelAndView.addObject("includedMacros", includedMacros);
            modelAndView.addObject("pageGrabber", freeMarkerUtil);
            if (!theme.getGaCode().isEmpty()) {
                modelAndView.addObject("googleAnalyticsTrackingCode", theme.getGaCode());
            }
            if (!theme.getAddThisCode().isEmpty()) {
                modelAndView.addObject("addThisTrackingCode", theme.getAddThisCode());
            }
        }
    }
}
