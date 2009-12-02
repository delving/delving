package eu.europeana.web.controller;

import eu.europeana.database.StaticInfoDao;
import eu.europeana.database.UserDao;
import eu.europeana.database.domain.CarouselItem;
import eu.europeana.database.domain.SavedItem;
import eu.europeana.database.domain.SearchTerm;
import eu.europeana.database.domain.User;
import eu.europeana.web.util.ControllerUtil;
import javax.servlet.http.HttpServletRequest;

/**
 * Remove something associated with a user
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class SaveEditorItemController extends AbstractAjaxController {

    private StaticInfoDao staticInfoDao;
    private UserDao userDao;

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void setStaticInfoDao(StaticInfoDao staticInfoDao) {
        this.staticInfoDao = staticInfoDao;
    }

    public boolean handleAjax(HttpServletRequest request) throws Exception {
        User user = ControllerUtil.getUser();
        String className = request.getParameter("className");
        String idString = request.getParameter("id");
        if (className == null || idString == null) {
            throw new IllegalArgumentException("Expected 'className' and 'id' parameters!");
        }
        Long id = Long.valueOf(idString);
        Class clazz = Class.forName("eu.europeana.database.domain."+className);

        //find object to be removed

        //check that the logged in user is the owner of the object

        //do remove
        if (clazz == SearchTerm.class) {
            // todo catch illegal argument exception return false
            SearchTerm searchTerm = staticInfoDao.addSearchTerm(id);
            if (searchTerm == null) {
                return false;
            }
        }
        else if (clazz == CarouselItem.class) {
            SavedItem savedItem = userDao.fetchSavedItemById(id);
            CarouselItem carouselItem = staticInfoDao.createCarouselItem(savedItem.getEuropeanaId().getEuropeanaUri(), savedItem.getId());
            if (carouselItem == null) {
                return false;
            }
            // old solution
//            user = staticInfoDao.addCarouselItem(user, id);
        }
        else {
            return false;
        }

        ControllerUtil.setUser(user);
        return true;
    }

}