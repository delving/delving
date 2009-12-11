package eu.europeana.web.controller;

import javax.servlet.http.HttpServletRequest;

/**
 * Class used for the Thought Lab Controller
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class ThougtLabController extends AbstractPortalController {

    public void handle(HttpServletRequest request, Model model) throws Exception {
        model.setView("thought-lab");
    }


}