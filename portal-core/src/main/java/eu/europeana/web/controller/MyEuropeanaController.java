package eu.europeana.web.controller;

import eu.europeana.database.UserDao;
import eu.europeana.database.domain.User;
import eu.europeana.web.util.ControllerUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MyEuropeanaController extends AbstractPortalController {

	private UserDao userDao;

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public void handle(HttpServletRequest request, Model model) throws Exception {
		model.setView("myeuropeana");
		User user = ControllerUtil.getUser();
		if (user != null) {
			ControllerUtil.setUser(userDao.updateUser(user));
		}
	}
}