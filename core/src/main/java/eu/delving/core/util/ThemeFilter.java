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

package eu.delving.core.util;

import eu.delving.core.storage.User;
import eu.europeana.core.util.web.ControllerUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * Put necessary things into thread local for subsequent use during handling of the request
 *
 * @author Gerald de Jong <gerald@delving.eu>
 */

public class ThemeFilter implements Filter {

    private static ThreadLocal<PortalTheme> themeThreadLocal = new ThreadLocal<PortalTheme>();
    private static ThreadLocal<LocalizedFieldNames.Lookup> lookupThreadLocal = new ThreadLocal<LocalizedFieldNames.Lookup>();

    @Autowired
    private ThemeHandler themeHandler;

    @Autowired
    private LocalizedFieldNames localizedFieldNames;

    /**
     * Fetch the theme from thread local, or fail loudly
     *
     * @return the theme
     */

    public static PortalTheme getTheme() {
        if (themeThreadLocal.get() == null) {
            Logger.getLogger(ThemeFilter.class).error(ThemeFilter.class+" must be installed!");
            return null;
        }
        return themeThreadLocal.get();
    }

    /**
     * Fetch the lookup from thread local, or fail loudly
     *
     * @return the lookup machine
     */

    public static LocalizedFieldNames.Lookup getLookup() {
        if (lookupThreadLocal.get() == null) {
            Logger.getLogger(ThemeFilter.class).error(ThemeFilter.class+" must be installed!");
            return null;
        }
        return lookupThreadLocal.get();
    }

     /**
     * This creates the default ModelAndView for the portal applications. It should be used in every Controller.
     *
     * @param view The Freemarker template that will be used by the model to render the view
     * @return ModelAndView page
     */

    public static ModelAndView createThemedModelAndViewPage(String view) {
        final String themeTemplateDir = getTheme().getTemplateDir();
        ModelAndView page = new ModelAndView(themeTemplateDir + "/" + view);
        User user = ControllerUtil.getUser();
        page.addObject("user", user);
        return page;
    }

    /**
     * Pretend that a request has come in for a theme.
     *
     * @param themeName which theme
     * @throws Exception oops
     */

    public void initialize(String themeName) throws Exception {
        final PortalTheme portalTheme = themeHandler.getByThemeName(themeName);
        themeThreadLocal.set(portalTheme);
        lookupThreadLocal.set(localizedFieldNames.createLookup(Arrays.asList(portalTheme.getLocaliseQueryKeys())));
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // not used
    }

    /**
     * Filter Servlet requests, putting Portal Theme and Lookup in thread local
     *
     * @param request from user
     * @param response to user
     * @param chain the other filters
     * @throws IOException problem
     * @throws ServletException problem
     */

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final PortalTheme portalTheme = themeHandler.getByRequest(request);
        themeThreadLocal.set(portalTheme);
        lookupThreadLocal.set(localizedFieldNames.createLookup(Arrays.asList(portalTheme.getLocaliseQueryKeys())));
        chain.doFilter(request, response);
        themeThreadLocal.remove();
        lookupThreadLocal.remove();
    }

    @Override
    public void destroy() {
        // not used
    }
}
