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

package eu.europeana.web.controller;

import eu.europeana.web.util.ControllerUtil;
import eu.europeana.web.util.StaticPageCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Genereric controller for static pages.
 *
 * @author Eric van der Meulen <eric.meulen@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

@Controller
public class StaticPageController {

    @Autowired
    private StaticPageCache staticPageCache;

    /**
     * All of the pages are served up from here
     *
     * @param pageName name of the page
     * @param request  where we find locale
     * @param response where to write it
     * @throws Exception something went wrong
     */

    @RequestMapping("/{pageName}.html")
    public void fetchStaticPage(
            @PathVariable("pageName") String pageName,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {
        String page = staticPageCache.getPage(pageName, ControllerUtil.getLocale(request));
        if (page == null) {
            response.sendRedirect("/error.html");
        }
        else {
            response.setContentLength(page.length());
            response.getWriter().write(page);
        }
    }

}