package eu.europeana.controller;

import eu.europeana.controller.util.ControllerUtil;
import eu.europeana.database.DashboardDao;
import eu.europeana.database.UserDao;
import eu.europeana.database.domain.CarouselItem;
import eu.europeana.database.domain.SavedItem;
import eu.europeana.database.domain.SearchTerm;
import eu.europeana.database.domain.User;
import javax.servlet.http.HttpServletRequest;

/**
 * Remove something associated with a user
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class SaveEditorItemController extends AbstractAjaxController {

    private UserDao userDao;

    public DashboardDao getDashboardDao() {
        return dashboardDao;
    }

    public void setDashboardDao(DashboardDao dashboardDao) {
        this.dashboardDao = dashboardDao;
    }

    private DashboardDao dashboardDao;

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
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
            SearchTerm searchTerm = userDao.addSearchTerm(id);
            if (searchTerm == null) {
                return false;
            }
        }
        else if (clazz == CarouselItem.class) {
            SavedItem savedItem = dashboardDao.fetchSavedItemById(id);
            CarouselItem carouselItem = dashboardDao.createCarouselItem(savedItem.getEuropeanaId().getEuropeanaUri(), savedItem.getId());
            if (carouselItem == null) {
                return false;
            }
            // old solution
//            user = userDao.addCarouselItem(user, id);
        }
        else {
            return false;
        }

        ControllerUtil.setUser(user);
        return true;
    }

}