package eu.europeana.controller;

import eu.europeana.controller.util.ControllerUtil;
import eu.europeana.database.StaticInfoDao;
import eu.europeana.database.domain.Language;
import eu.europeana.database.domain.StaticPage;
import eu.europeana.database.domain.StaticPageType;
import javax.servlet.http.HttpServletRequest;

/**
 * Genereric controller for static pages. The view is injected in the dispatcher servlet
 *
 * @author Eric van der Meulen <eric.meulen@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 *
 */

public class StaticPageController extends AbstractPortalController {

    private StaticPageType pageType;
    private StaticInfoDao staticInfoDao;
    private boolean notLoadableFromDb;

    public void setTemplate(String template) {
        this.pageType = StaticPageType.get(template);
    }

    public void handle(HttpServletRequest request, Model model) throws Exception {
        if (!notLoadableFromDb) {
            Language language = ControllerUtil.getLocale(request);
            StaticPage staticPage = staticInfoDao.getStaticPage(pageType, language);
            model.put("staticPage", staticPage);
        }
        model.setContentType("text/html; charset=utf-8");
        model.setView(pageType.getViewName());
    }

     public void setStaticInfoDao(StaticInfoDao staticInfoDao) {
        this.staticInfoDao = staticInfoDao;
    }
    public void setNotLoadableFromDb(boolean notLoadableFromDb) {
        this.notLoadableFromDb = notLoadableFromDb;
    }
}