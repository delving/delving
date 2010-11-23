package eu.delving.services.controller;

import eu.delving.services.search.OpenSearchService;
import eu.europeana.core.BeanQueryModelFactory;
import eu.europeana.core.util.web.ClickStreamLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Properties;

/**
 * todo: take another good look at this
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Mar 17, 2010 3:00:23 PM
 */

@Controller
public class OpenSearchController {

    @Autowired
    private ClickStreamLogger clickStreamLogger;

    @Autowired
    private BeanQueryModelFactory beanQueryModelFactory;

    @Autowired
    private Properties launchProperties;

    @RequestMapping("/api/open-search")
    public void searchServiceController(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(OpenSearchService.parseHttpServletRequest(request, beanQueryModelFactory, launchProperties));
        response.getWriter().close();
    }

    @RequestMapping("/api/open-search.xml")
    public void searchServiceDescriptionController(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(OpenSearchService.renderDescriptionDocument(request, beanQueryModelFactory, launchProperties));
        response.getWriter().close();
    }
}
