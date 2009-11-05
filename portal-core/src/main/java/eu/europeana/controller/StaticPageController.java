package eu.europeana.controller;

import eu.europeana.controller.util.ControllerUtil;
//import eu.europeana.database.MessageDao;
import eu.europeana.database.StaticInfoDao;
import eu.europeana.database.domain.Language;
import eu.europeana.database.domain.StaticPage;

import javax.servlet.http.HttpServletRequest;

/**
 * Genereric controller for static pages. The view is injected in the dispatcher servlet
 *
 * @author Eric van der Meulen <eric.meulen@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class StaticPageController extends AbstractPortalController {

    private String template;
    //private MessageDao messageDao;
    private StaticInfoDao staticInfoDao;
    private boolean notLoadableFromDb;

    public void setTemplate(String template) {
        this.template = template;
    }

    public void handle(HttpServletRequest request, Model model) throws Exception {
        if (!notLoadableFromDb) {
            Language language = ControllerUtil.getLocale(request);
            //StaticPage staticPage = messageDao.fetchStaticPage(language, template);
            StaticPage staticPage = staticInfoDao.fetchStaticPage(language, template);
            model.put("staticPage", staticPage);
        }
        model.setContentType("text/html; charset=utf-8");
        model.setView(template);
    }
    /*
    public void setMessageDao(MessageDao messageDao) {
        this.messageDao = messageDao;
    }
           */
     public void setStaticInfoDao(StaticInfoDao staticInfoDao) {
        this.staticInfoDao = staticInfoDao;
    }
    public void setNotLoadableFromDb(boolean notLoadableFromDb) {
        this.notLoadableFromDb = notLoadableFromDb;
    }
}