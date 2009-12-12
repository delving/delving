package eu.europeana.web.controller;

import eu.europeana.database.StaticInfoDao;
import eu.europeana.database.domain.Language;
import eu.europeana.database.domain.StaticPage;
import eu.europeana.database.domain.StaticPageType;
import eu.europeana.web.util.ControllerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * Genereric controller for static pages. The view is injected in the dispatcher servlet
 *
 * @author Eric van der Meulen <eric.meulen@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

@Controller
public class StaticPageController {

    @Autowired
    private StaticInfoDao staticInfoDao;

    public void setStaticInfoDao(StaticInfoDao staticInfoDao) {
        this.staticInfoDao = staticInfoDao;
    }


    /*
    * freemarker template not loadable from database
    */

    @RequestMapping("/advancedsearch.html")
    public ModelAndView AdvancedSearchHandler(HttpServletRequest request) throws Exception {
        StaticPageType pageType = StaticPageType.ADVANCED_SEARCH;
        return ControllerUtil.createModelAndViewPage(pageType.getViewName());
    }


    /*
     * freemarker Template not loadable from database
     */

    @RequestMapping("/error.html")
    public ModelAndView ErrorPageHandler(HttpServletRequest request) throws Exception {
        StaticPageType pageType = StaticPageType.ERROR;
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
        return page;
    }
}