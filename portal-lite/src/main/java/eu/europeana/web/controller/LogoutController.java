package eu.europeana.web.controller;

import javax.servlet.http.HttpServletRequest;

/**
 * Where we say goodbye
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class LogoutController extends AbstractPortalController {
    public void handle(HttpServletRequest request, Model model) throws Exception {
        model.setView("logout");
    }
}