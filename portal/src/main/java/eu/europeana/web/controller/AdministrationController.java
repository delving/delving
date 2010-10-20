/*
 * Copyright 2007 EDL FOUNDATION
 *
 *  Licensed under the EUPL, Version 1.0 or? as soon they
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

import eu.europeana.core.database.UserDao;
import eu.europeana.core.database.domain.Role;
import eu.europeana.core.database.domain.User;
import eu.europeana.core.util.web.ControllerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * This controller is to offer a user with ROLE_ADMINISTRATOR the ability to adjust various things
 * about other users that they select.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@Controller
@RequestMapping("/administration.html")
public class AdministrationController {

    @Autowired
    private UserDao userDao;

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView adminGet(
            @ModelAttribute("command") AdminForm adminForm

    ) throws Exception {
        return ControllerUtil.createModelAndViewPage("administration");
    }

    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView adminPost(
            AdminForm adminForm
    ) throws Exception {
        ModelAndView page = ControllerUtil.createModelAndViewPage("administration");
        if (adminForm.getUserEmail().isEmpty()) {
            List<User> userList = userDao.fetchUsers(adminForm.getSearchPattern().trim());
            page.addObject("userList", userList);
        }
        else {
            User targetUser = userDao.fetchUserByEmail(adminForm.getUserEmail());
            if (targetUser == null) {
                throw new IllegalArgumentException(String.format("User %s not found", adminForm.getUserEmail()));
            }
            if (!adminForm.getNewRole().isEmpty()) {
                Role role = Role.valueOf(adminForm.getNewRole());
                switch (role) {
                    case ROLE_RESEARCH_USER:
                        targetUser.setRole(role);
                        userDao.updateUser(targetUser);
                        page.addObject("targetUser", targetUser);
                        break;
                    case ROLE_ADMINISTRATOR:
                        if (ControllerUtil.getUser().getRole() != Role.ROLE_GOD) {
                            throw new IllegalArgumentException("Only superuser can make someone an administrator");
                        }
                        targetUser.setRole(role);
                        userDao.updateUser(targetUser);
                        page.addObject("targetUser", targetUser);
                        break;
                    default:
                        break;
                }
            }
        }
        return page;
    }


    public static class AdminForm {
        private String searchPattern = "";
        private String userEmail = "";
        private String newRole = "";

        public String getSearchPattern() {
            return searchPattern;
        }

        public void setSearchPattern(String searchPattern) {
            this.searchPattern = searchPattern;
        }

        public String getUserEmail() {
            return userEmail;
        }

        public void setUserEmail(String userEmail) {
            this.userEmail = userEmail;
        }

        public String getNewRole() {
            return newRole;
        }

        public void setNewRole(String newRole) {
            this.newRole = newRole;
        }
    }
}