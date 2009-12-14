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

import eu.europeana.database.StaticInfoDao;
import eu.europeana.database.domain.Language;
import eu.europeana.database.domain.StaticPage;
import eu.europeana.database.domain.StaticPageType;
import eu.europeana.query.ClickStreamLogger;
import eu.europeana.web.util.ControllerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * Genereric controller for static pages.
 *
 * @author Eric van der Meulen <eric.meulen@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

@Controller
public class StaticPageController {

    @Autowired
    private StaticInfoDao staticInfoDao;

    @Autowired
    private ClickStreamLogger clickStreamLogger;

    /*
    * freemarker template not loadable from database
    */

    @RequestMapping("/advancedsearch.html")
    public ModelAndView AdvancedSearchHandler(HttpServletRequest request) throws Exception {
        StaticPageType pageType = StaticPageType.ADVANCED_SEARCH;
        clickStreamLogger.log(request, pageType);
        return ControllerUtil.createModelAndViewPage(pageType.getViewName());
    }

    /*
     * freemarker Template not loadable from database
     */

    @RequestMapping("/error.html")
    public ModelAndView ErrorPageHandler(HttpServletRequest request) throws Exception {
        StaticPageType pageType = StaticPageType.ERROR;
        clickStreamLogger.log(request, pageType);
        return ControllerUtil.createModelAndViewPage(pageType.getViewName());
    }

    /*
     * freemarker template loadable from database
     */

    @RequestMapping("/aboutus.html")
    public ModelAndView AboutUsPageHandler(HttpServletRequest request) throws Exception {
        StaticPageType pageType = StaticPageType.ABOUT_US;
        return loadablePageFromDB(
                request,
                pageType,
                ControllerUtil.createModelAndViewPage(pageType.getViewName())
        );
    }

    private ModelAndView loadablePageFromDB(HttpServletRequest request, StaticPageType pageType, ModelAndView page) {
        Language language = ControllerUtil.getLocale(request);
        StaticPage staticPage = staticInfoDao.getStaticPage(pageType, language);
        page.addObject("staticPage", staticPage);
        clickStreamLogger.log(request, pageType);
        return page;
    }
}