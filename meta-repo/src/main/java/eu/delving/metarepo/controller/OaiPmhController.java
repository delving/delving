package eu.delving.metarepo.controller;

import eu.delving.metarepo.core.MetaRepo;
import eu.delving.metarepo.harvesting.OaiPmhParser;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jun 15, 2010 12:17:29 AM
 */
@Controller
public class OaiPmhController {

    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private MetaRepo metaRepo;


    @RequestMapping("/oai-pmh")
    public
    @ResponseBody
    String searchController(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("text/xml");
        return OaiPmhParser.parseHttpServletRequest(request, metaRepo);
    }
}
