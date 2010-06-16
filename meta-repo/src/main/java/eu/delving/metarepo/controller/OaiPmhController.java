package eu.delving.metarepo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jun 15, 2010 12:17:29 AM
 */
@Controller
public class OaiPmhController {

    @RequestMapping("/oai-pmh")
    public ModelAndView searchController(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("text/xml");
        return null;
    }
}
