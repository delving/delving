package eu.delving.services.controller;

import eu.delving.core.binding.SolrBindingService;
import eu.delving.services.search.OpenSearchService;
import eu.europeana.core.BeanQueryModelFactory;
import eu.europeana.core.querymodel.query.BriefDoc;
import eu.europeana.core.querymodel.query.QueryType;
import eu.europeana.core.util.web.ClickStreamLogger;
import eu.europeana.core.util.web.ControllerUtil;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

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

    private String convertOpenSearchQueryToLuceneQuery(HttpServletRequest request ) {
        // todo add code here to convert open search query to lucene query
        return "*:*";
    }

    @Deprecated
    @RequestMapping("/open-search.html")
    public ModelAndView searchController(HttpServletRequest request) throws Exception {
        // create model and view
        ModelAndView mav = ControllerUtil.createModelAndViewPage("open-search.xml");
        // get open search parameters from uri +  convert them to Lucene query language
        String convertLuceneQuery = convertOpenSearchQueryToLuceneQuery(request);
        // extract human readible query, this is the open search query (not the luceneQuery)
        String queryString = "*:*";
        // create solr query
        SolrQuery solrQuery = new SolrQuery()
                .setQuery(convertLuceneQuery)
                .setRows(12)
                .setQueryType(QueryType.ADVANCED_QUERY.toString());
        // get response from solr
        QueryResponse response = beanQueryModelFactory.getSolrResponse(solrQuery);
        // bind response to briefDoc and add model
        List<? extends BriefDoc> briefDocList = SolrBindingService.getBriefDocs(response);
        mav.addObject("briefDocList", briefDocList);
        return mav;
    }

    @RequestMapping("/api/open-search")
    public void searchServiceController(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(OpenSearchService.parseHttpServletRequest(request, beanQueryModelFactory));
        response.getWriter().close();
    }

}
