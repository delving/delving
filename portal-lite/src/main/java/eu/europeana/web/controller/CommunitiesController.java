package eu.europeana.web.controller;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Eric van der Meulen <eric.meulen@gmail.com>
 */

public class CommunitiesController extends AbstractPortalController {
    public void handle(HttpServletRequest request, Model model) throws Exception {
        model.setView("communities");
        String page = request.getParameter("page");
        if (page != null && page.equals("list")) {
            model.setView("communities-list");
        }
        if (page != null && page.equals("view")) {
            model.setView("communities-view");
        }
        if (page != null && page.equals("members")) {
            model.setView("communities-members");
        }
        if (page != null && page.equals("links")) {
            model.setView("communities-links");
        }
    }
}