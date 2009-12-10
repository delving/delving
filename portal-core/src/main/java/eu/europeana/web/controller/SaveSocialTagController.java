package eu.europeana.web.controller;

import eu.europeana.database.UserDao;
import eu.europeana.database.domain.SocialTag;
import eu.europeana.database.domain.User;
import eu.europeana.query.DocType;
import eu.europeana.web.util.ControllerUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * When somebody wants to save an item they have found.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class SaveSocialTagController extends AbstractAjaxController {
    private UserDao userDao;

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public boolean handleAjax(HttpServletRequest request) throws Exception {
        SocialTag socialTag = new SocialTag();
        socialTag.setTag(getStringParameter("tag", request));
        socialTag.setEuropeanaUri(getStringParameter("europeanaUri", request));
        socialTag.setDocType(DocType.valueOf(getStringParameter("docType", request)));
        socialTag.setEuropeanaObject(getStringParameter("europeanaObject", request));
        socialTag.setTitle(getStringParameter("title", request));
        User user = ControllerUtil.getUser();
        socialTag.setLanguage(ControllerUtil.getLocale(request));
        user = userDao.addSocialTag(user, socialTag);
        ControllerUtil.setUser(user);
        return true;
    }
}